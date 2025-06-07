package net.cycastic.portfoliotoolkit.domain.repository.listing;

import net.cycastic.portfoliotoolkit.domain.model.listing.TextListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextListingRepository extends JpaRepository<TextListing, Integer> {
}
