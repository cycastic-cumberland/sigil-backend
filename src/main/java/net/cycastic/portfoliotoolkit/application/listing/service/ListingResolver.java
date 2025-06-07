package net.cycastic.portfoliotoolkit.application.listing.service;

import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import net.cycastic.portfoliotoolkit.dto.listing.ListingDto;

import java.util.Collection;
import java.util.Map;

public interface ListingResolver {
    void resolve(Map<Integer, Listing> listings, Map<Integer, ListingDto> collector);
}
