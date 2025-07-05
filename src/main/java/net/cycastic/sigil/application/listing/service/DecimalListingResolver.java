package net.cycastic.sigil.application.listing.service;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.model.ListingType;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.repository.listing.DecimalListingRepository;
import net.cycastic.sigil.domain.dto.listing.DecimalListingDto;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Lazy
@Component
@RequiredArgsConstructor
public class DecimalListingResolver implements ListingResolver {
    private final DecimalListingRepository repository;

    @Override
    public void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector) {
        var ids = listings.values().stream()
                .filter(l -> l.getType() == ListingType.DECIMAL)
                .map(Listing::getId)
                .toList();
        var filteredListings = repository.findAllById(ids);
        for (var l : filteredListings){
            collector.put(l.getId(), DecimalListingDto.fromDomain(l, listings.get(l.getId())));
        }
    }

    @Override
    public Optional<ListingDto> resolve(Listing listing) {
        if (listing.getType() != ListingType.DECIMAL){
            return Optional.empty();
        }

        return Optional.of(DecimalListingDto.fromDomain(listing.getDecimalListing(), listing));
    }
}
