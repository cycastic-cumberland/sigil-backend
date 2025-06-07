package net.cycastic.portfoliotoolkit.application.listing.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import net.cycastic.portfoliotoolkit.domain.model.ListingAccessControlPolicy;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingACPRepository;
import net.cycastic.portfoliotoolkit.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.dto.paging.PageResponseDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingService {
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);
    private static final Pattern SPECIAL_CHARACTERS_ESCAPE_PATTERN = Pattern.compile("[\\\\%_]");
    private static final Pattern SPECIAL_CHARACTERS_UNESCAPE_PATTERN = Pattern.compile("\\\\\\\\|\\\\%|\\\\_");

    private final ListingACPRepository listingACPRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final List<ListingResolver> resolvers;

    public List<ListingAccessControlPolicy> getOrderedPolicies(@NonNull Listing listing){
        var project = listing.getProject();
        return listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
    }

    private static boolean isRangeEncapsulated(byte @NonNull[] rangeLow, byte @NonNull[] rangeHigh, byte[] boundLow, byte[] boundHigh){
        if (boundLow != null && NsoUtilities.compareByteArrays(rangeLow, boundLow) < 0){
            return false;
        }
        if (boundHigh != null && NsoUtilities.compareByteArrays(rangeHigh, boundHigh) >= 0){
            return false;
        }
        return true;
    }

    public void verifyAccess(@NonNull Project project, byte @NonNull[] lowSearchKey, byte @NonNull[] highSearchKey){
        if (loggedUserAccessor.isAdmin()){
            return;
        }

        if (project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }
        var currentUserId = loggedUserAccessor.tryGetUserId();
        var policies = listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
        for (var policy : policies){
            if (!(policy.getApplyToId() == null ||
                    (currentUserId.isPresent() && policy.getApplyToId().equals(currentUserId.get())))){
                continue;
            }

            if (!isRangeEncapsulated(lowSearchKey, highSearchKey, policy.getLowSearchKey(), policy.getHighSearchKey())){
                continue;
            }

            if (policy.isAllowed()){
                // Returns to allow
                return;
            }

            // Breaks to throw
            break;
        }

        // Forbids if exhaust all policies
        throw new ForbiddenException();
    }

    public static byte[] encodeSearchKey(@NonNull String path){
        var matcher = SPECIAL_CHARACTERS_ESCAPE_PATTERN.matcher(path);
        var result = matcher.replaceAll(match -> switch (match.group()) {
            case "\\" -> "\\\\\\\\";
            case "%" -> "\\\\%";
            case "_" -> "\\\\_";
            default -> match.group();
        });
        var parts = result.split("/");
        var buffer = ByteBuffer.allocate(NsoUtilities.KEY_LENGTH * NsoUtilities.SEPARATOR_SEQUENCE_LENGTH);

        NsoUtilities.insertSeparator(buffer);
        var first = true;
        for (var part : parts){
            if (part.isEmpty()){
                continue;
            }
            if (first){
                first = false;
            } else {
                NsoUtilities.insertSeparator(buffer);
            }
            buffer.put(part.getBytes(StandardCharsets.UTF_8));
        }

        var finalArray = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, finalArray, 0, buffer.position());
        return finalArray;
    }

    public static String decodeSearchKey(byte @NonNull [] searchKey){
        var sb = new StringBuilder();
        var delimeterAsString = NsoUtilities.delimeterAsString();
        for (var part : NsoUtilities.split(searchKey)){
            if (part.length == 0){
                continue;
            }

            var matcher = SPECIAL_CHARACTERS_UNESCAPE_PATTERN.matcher(new String(part, StandardCharsets.UTF_8));
            var result = matcher.replaceAll(match -> switch (match.group()) {
                case "\\\\\\\\" -> "\\";
                case "\\\\%" -> "%";
                case "\\\\_" -> "_";
                default -> match.group().equals(delimeterAsString) ? "" : match.group();
            });
            sb.append('/').append(result);
        }

        return sb.toString();
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
}
