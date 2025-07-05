package net.cycastic.sigil.domain.repository.listing;

import net.cycastic.sigil.domain.model.listing.DecimalListing;
import net.cycastic.sigil.domain.model.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecimalListingRepository extends JpaRepository<DecimalListing, Integer> {
    void removeByListing(Listing listing);
}
