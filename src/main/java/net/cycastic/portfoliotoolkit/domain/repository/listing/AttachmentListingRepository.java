package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
