package net.cycastic.portfoliotoolkit.domain.repository.listing;

import net.cycastic.portfoliotoolkit.domain.model.listing.DecimalListing;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecimalListingRepository extends JpaRepository<DecimalListing, Integer> {
    void removeByListing(Listing listing);
}
