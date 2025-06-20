package net.cycastic.portfoliotoolkit.application.listing.service;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import net.cycastic.portfoliotoolkit.domain.repository.listing.TextListingRepository;
import net.cycastic.portfoliotoolkit.domain.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.domain.dto.listing.TextListingDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Lazy
@Component
@RequiredArgsConstructor
public class TextListingResolver implements ListingResolver {
    private final TextListingRepository repository;

    @Override
    public void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector) {
        var ids = listings.values().stream()
                .filter(l -> l.getType() == ListingType.TEXT)
                .map(Listing::getId)
                .toList();
        var filteredListings = repository.findAllById(ids);
        for (var l : filteredListings){
            collector.put(l.getId(), TextListingDto.fromDomain(l, listings.get(l.getId())));
        }
    }

    @Override
    public Optional<ListingDto> resolve(Listing listing) {
        if (listing.getType() != ListingType.TEXT){
            return Optional.empty();
        }

        return Optional.of(TextListingDto.fromDomain(listing.getTextListing(), listing));
    }
}
