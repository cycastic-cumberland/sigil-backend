package net.cycastic.sigil.application.listing.service;

import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.dto.listing.ListingDto;

import java.util.Map;
import java.util.Optional;

public interface ListingResolver {
    void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector);

    Optional<ListingDto> resolve(Listing listing);
}
