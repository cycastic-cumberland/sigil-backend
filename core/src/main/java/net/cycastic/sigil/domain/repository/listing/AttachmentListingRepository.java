package net.cycastic.sigil.domain.repository.listing;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.tenant.Tenant;
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

    Optional<AttachmentListing> findByListing_PartitionAndListing_ListingPath(@NotNull Partition partition, @NotNull String listingListingPath);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l.attachmentListing FROM Listing l WHERE l = :listing")
    Optional<AttachmentListing> findAttachmentListingForUpdate(@Param("listing") Listing listing);

    AttachmentListing findByListing(@NotNull Listing listing);

    @Query(value = """
                   SELECT l.id AS id, l.objectKey AS key, l.bucketName AS bucket
                   FROM AttachmentListing l
                   WHERE l.listing.partition.tenant = :tenant AND l.uploadCompleted
                   """,
            countQuery = """
                         SELECT COUNT(l.objectKey) FROM AttachmentListing l WHERE l.listing.partition.tenant = :tenant
                         """
    )
    Page<ObjectInfo> getObjectKeysByUser(@Param("user") Tenant tenant, Pageable pageable);
}
