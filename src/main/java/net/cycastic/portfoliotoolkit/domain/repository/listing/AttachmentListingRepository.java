package net.cycastic.portfoliotoolkit.domain.repository.listing;

import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentListingRepository extends JpaRepository<AttachmentListing, Integer> {
}
