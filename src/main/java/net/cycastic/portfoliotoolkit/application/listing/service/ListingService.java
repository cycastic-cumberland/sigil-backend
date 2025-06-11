package net.cycastic.portfoliotoolkit.application.listing.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.validation.constraints.NotNull;
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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingService {
    private static final Cache<String, Pattern> PATTERN_CACHE = Caffeine.newBuilder()
            .maximumSize(512) // TODO: Ways to override this
            .build();
    private static final Pattern MATCH_ALL = Pattern.compile(".*");
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingACPRepository listingACPRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final List<ListingResolver> resolvers;

    public List<ListingAccessControlPolicy> getOrderedPolicies(@NotNull Listing listing){
        var project = listing.getProject();
        return listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
    }

    private static boolean isRangeEncapsulated(byte @NotNull[] rangeLow, byte @NotNull[] rangeHigh, byte[] boundLow, byte[] boundHigh){
        if (boundLow != null && NsoUtilities.compareByteArrays(rangeLow, boundLow) < 0){
            return false;
        }
        if (boundHigh != null && NsoUtilities.compareByteArrays(rangeHigh, boundHigh) >= 0){
            return false;
        }
        return true;
    }

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
                             @NotNull Stream<Listing> listings){
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

        var it = listings.iterator();
        var allowed = 0;
        var iterated = 0;
        while (it.hasNext()){
           var listing = it.next();
           iterated++;
           if (allowPattern.matcher(listing.getListingPath()).matches()){
               allowed++;
               continue;
           }

           break;
        }
        if (allowed != iterated){
            throw new ForbiddenException();
        }
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
