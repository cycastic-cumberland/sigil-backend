package net.cycastic.sigil.domain.repository.listing;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.Project;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.model.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AttachmentListingRepository extends JpaRepository<AttachmentListing, Integer> {
    interface ObjectInfo {
        int getId();
        String getKey();
        String getBucket();
    }

    void removeByUploadCompletedAndListing_CreatedAtLessThan(boolean uploadCompleted, @NotNull OffsetDateTime listingCreatedAt);

    Optional<AttachmentListing> findByListing_ProjectAndListing_ListingPath(@NotNull Project listingProject, @NotNull String listingListingPath);

    Optional<AttachmentListing> findByListing_Project_IdAndListing_ListingPath(@NotNull Integer listingProjectId, @NotNull String listingListingPath);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l.attachmentListing FROM Listing l WHERE l = :listing")
    Optional<AttachmentListing> findAttachmentListingForUpdate(@Param("listing") Listing listing);

    AttachmentListing findByListing(@NotNull Listing listing);

    @Query(value = """
                   SELECT l.id AS id, l.objectKey AS key, l.bucketName AS bucket
                   FROM AttachmentListing l
                   WHERE l.listing.project.user = :user AND l.uploadCompleted
                   """,
            countQuery = """
                         SELECT COUNT(l.objectKey) FROM AttachmentListing l WHERE l.listing.project.user = :user
                         """
    )
    Page<ObjectInfo> getObjectKeysByUser(@Param("user")User user, Pageable pageable);
}
