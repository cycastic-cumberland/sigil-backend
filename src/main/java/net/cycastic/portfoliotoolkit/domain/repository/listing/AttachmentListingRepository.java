package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentListingRepository extends JpaRepository<AttachmentListing, Integer> {
    boolean existsByListing_Project_AndUploadCompleted(@NotNull Project listingProject, boolean uploadCompleted);

    Page<AttachmentListing> findByListing_Project_AndUploadCompleted(@NotNull Project listingProject, boolean uploadCompleted, Pageable pageable);

    Optional<AttachmentListing> findAttachmentListingByListing_Project(@NotNull Project listingProject);
}
