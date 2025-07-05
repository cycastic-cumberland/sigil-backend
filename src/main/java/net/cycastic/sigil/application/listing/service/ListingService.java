package net.cycastic.sigil.application.listing.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.S3Configurations;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.ListingType;
import net.cycastic.sigil.domain.model.Project;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.model.ListingAccessControlPolicy;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.domain.repository.listing.*;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.DeferrableStorageProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingService {
    public static final String TEMP_FILE_PREFIX = "tmp/";

    private static final Cache<String, Pattern> PATTERN_CACHE = Caffeine.newBuilder()
            .maximumSize(512) // TODO: Ways to override this
            .build();
    private static final Pattern MATCH_ALL = Pattern.compile(".*");

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingACPRepository listingACPRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final List<ListingResolver> resolvers;
    private final StorageProvider storageProvider;
    private final DeferrableStorageProvider deferrableStorageProvider;
    private final UserRepository userRepository;

    private final TextListingRepository textListingRepository;
    private final DecimalListingRepository decimalListingRepository;
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;
    private final S3Configurations s3Configurations;

    private static Pattern buildRegexFromGlob(@NotNull String glob){
        var regex = new StringBuilder("^");
        var i = 0;
        while (i < glob.length()) {
            var c = glob.charAt(i);
            if (c == '*') {
                if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                    regex.append(".*");
                    i += 2;
                } else {
                    regex.append("[^/]*");
                    i++;
                }
            } else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
                regex.append("\\").append(c);
                i++;
            } else {
                regex.append(c);
                i++;
            }
        }
        return Pattern.compile(regex.append('$').toString());
    }

    private static Pattern getRegex(@NotNull ListingAccessControlPolicy policy){
        if (policy.getGlobPath() == null){
            return MATCH_ALL;
        }
        return PATTERN_CACHE.get(policy.getGlobPath(), ListingService::buildRegexFromGlob);
    }

    private static Pattern joinPatterns(@NotNull Stream<Pattern> stream){
        var combined = stream.map(Pattern::pattern)
                .map(s -> "(?:" + s + ")")
                .collect(Collectors.joining("|"));
        var pattern = Pattern.compile(combined);
        PATTERN_CACHE.put(combined, pattern);
        return pattern;
    }

    public void verifyAccess(@NotNull Project project,
                             @NotNull Stream<String> listingPaths){
        if (loggedUserAccessor.isAdmin()){
            return;
        }

        if (project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }
        var currentUserId = loggedUserAccessor.tryGetUserId();
        var policies = listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
        var baseFilter = policies.stream()
                .filter(policy -> policy.getApplyToId() == null ||
                        (currentUserId.isPresent() && policy.getApplyToId().equals(currentUserId.get())));
        var allowPattern = joinPatterns(baseFilter.filter(ListingAccessControlPolicy::isAllowed)
                .map(ListingService::getRegex));

        try (listingPaths){
            var it = listingPaths.iterator();
            var allowed = 0;
            var iterated = 0;
            while (it.hasNext()){
               var listingPath = it.next();
               iterated++;
               if (allowPattern.matcher(listingPath).matches()){
                   allowed++;
                   continue;
               }

               break;
            }
            if (allowed != iterated){
                throw new ForbiddenException();
            }
        }
    }

    public ListingDto toDto(Listing listing){
        for (var resolver : resolvers){
            var opt = resolver.resolve(listing);
            if (opt.isPresent()){
                return opt.get();
            }
        }

        // TODO: provide more details
        throw new RequestException(400, "Listing is broken");
    }

    public PageResponseDto<ListingDto> toDto(Page<Listing> listings){
        var domainMap = listings.getContent().stream()
                .collect(Collectors.toMap(Listing::getId, l -> l));
        HashMap<Integer, ListingDto> dtoMap = HashMap.newHashMap(domainMap.size());
        for (var resolver : resolvers){
            resolver.resolve(domainMap, dtoMap);
            if (dtoMap.size() == domainMap.size()){
                break;
            }
        }

        if (dtoMap.size() != domainMap.size()){
            // TODO: provide more details
            logger.error("Listing are broken");
        }

        var items = listings.getContent().stream()
                .map(l -> dtoMap.get(l.getId()))
                .toList();
        PageResponseDto.PageResponseDtoBuilder<ListingDto> builder = PageResponseDto.builder();
        builder.items(items)
                .page(listings.getNumber() + 1)
                .pageSize(listings.getSize())
                .totalPages(listings.getTotalPages())
                .totalElements((int)listings.getTotalElements());

        return builder.build();
    }

    @Transactional
    public AttachmentListing saveTemporaryAttachment(@NotNull Project project, @NotNull String path, String mimeType){
        var listing = Listing.builder()
                .project(project)
                .listingPath(path)
                .type(ListingType.ATTACHMENT)
                .createdAt(OffsetDateTime.now())
                .build();
        var fileExt = FilenameUtils.getExtension(path);
        var attachmentListing = AttachmentListing.builder()
                .listing(listing)
                .bucketName(s3Configurations.getAttachmentBucketName())
                .bucketRegion(s3Configurations.getRegionName())
                .objectKey(fileExt.isEmpty() ? UUID.randomUUID().toString() : String.format("%s%s.%s", TEMP_FILE_PREFIX, UUID.randomUUID(), fileExt))
                .mimeType(mimeType)
                .shareToken(UUID.randomUUID())
                .build();
        listingRepository.save(listing);
        attachmentListingRepository.save(attachmentListing);
        return attachmentListing;
    }

    private void deleteListingNoTransaction(Listing listing, AttachmentListing attachment){
        if (attachment.isUploadCompleted()){
            attachment.setUploadCompleted(false);
            attachmentListingRepository.save(attachment);

            var size = storageProvider.getBucket(attachment.getBucketName()).getObjectSize(attachment.getObjectKey());
            var user = userRepository.findByListing(listing)
                    .orElseThrow(() -> new RequestException(404, "User not found"));
            user.setAccumulatedAttachmentStorageUsage(user.getAccumulatedAttachmentStorageUsage() - size);
            deferrableStorageProvider.getBucket(attachment.getBucketName()).deleteFile(attachment.getObjectKey());
            userRepository.save(user);
        }

        attachmentListingRepository.delete(attachment);
        listingRepository.delete(listing);
    }

    public void deleteListingNoTransaction(int projectId, String path){
        var listing = listingRepository.findByProject_IdAndListingPath(projectId, path)
                .orElseThrow(() -> new RequestException(404, "Listing not found"));

        if (!listing.getProject().getId().equals(loggedUserAccessor.getProjectId())){
            throw new ForbiddenException();
        }

        switch (listing.getType()){
            case TEXT -> {
                textListingRepository.removeByListing(listing);
                listingRepository.delete(listing);
            }
            case DECIMAL -> {
                decimalListingRepository.removeByListing(listing);
                listingRepository.delete(listing);
            }
            case ATTACHMENT -> {
                var attachment = attachmentListingRepository.findAttachmentListingForUpdate(listing)
                        .orElseThrow(() -> new IllegalStateException("unreachable"));
                deleteListingNoTransaction(listing, attachment);
            }
        }
    }
}
