package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentListingRepository extends JpaRepository<AttachmentListing, Integer> {
    void removeByListing(Listing listing);

    boolean existsByListing_Project_AndUploadCompleted(@NotNull Project listingProject, boolean uploadCompleted);

    Page<AttachmentListing> findByListing_Project_AndUploadCompleted(@NotNull Project listingProject, boolean uploadCompleted, Pageable pageable);

    Optional<AttachmentListing> findByListing_ProjectAndListing_ListingPath(@NotNull Project listingProject, @NotNull String listingListingPath);
}
