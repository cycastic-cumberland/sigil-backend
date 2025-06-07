package net.cycastic.portfoliotoolkit.application.listing.service;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.dto.listing.AttachmentDto;
import net.cycastic.portfoliotoolkit.dto.listing.ListingDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Lazy
@Component
@RequiredArgsConstructor
public class AttachmentListingResolver implements ListingResolver {
    private final AttachmentListingRepository repository;

    @Override
    public void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector) {
        var ids = listings.values().stream()
                .filter(l -> l.getType() == ListingType.TEXT)
                .map(Listing::getId)
                .toList();
        var filteredListings = repository.findAllById(ids);
        for (var l : filteredListings){
            collector.put(l.getId(), AttachmentDto.fromDomain(l, listings.get(l.getId())));
        }
    }
}
