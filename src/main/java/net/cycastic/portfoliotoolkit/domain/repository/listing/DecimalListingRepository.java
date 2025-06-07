package net.cycastic.portfoliotoolkit.domain.repository.listing;

import net.cycastic.portfoliotoolkit.domain.model.listing.DecimalListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecimalListingRepository extends JpaRepository<DecimalListing, Integer> {
}
