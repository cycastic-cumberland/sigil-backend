package net.cycastic.sigil.domain.repository.listing;

import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.model.listing.TextListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextListingRepository extends JpaRepository<TextListing, Integer> {
    void removeByListing(Listing listing);
}
