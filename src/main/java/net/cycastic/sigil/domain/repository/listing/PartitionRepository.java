package net.cycastic.sigil.domain.repository.listing;

import net.cycastic.sigil.domain.model.Partition;
import net.cycastic.sigil.domain.model.Tenant;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PartitionRepository extends JpaRepository<Partition, Integer> {
    interface FileItem {
        String getName();
        boolean getIsPartition();
        OffsetDateTime getModifiedAt();
    }

    Optional<Partition> findByTenant_IdAndPartitionPath(int tenantId, String partitionPath);

    @Query("SELECT al.listing.partition FROM AttachmentListing al WHERE al = :attachmentListing")
    Partition findByAttachmentListing(@Param("attachmentListing")AttachmentListing attachmentListing);

    @Query(value = """
                   SELECT DISTINCT
                   CASE
                      WHEN LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN SUBSTRING(pu.partition.partitionPath, LENGTH(:folder) + 1)
                      ELSE SUBSTRING(pu.partition.partitionPath, LENGTH(:folder) + 1, LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                   END AS name,
                   CASE
                      WHEN LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN TRUE
                      ELSE FALSE
                   END AS isPartition,
                   CASE
                      WHEN LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN CASE
                                  WHEN pu.partition.updatedAt IS NULL THEN pu.partition.createdAt
                                  ELSE pu.partition.updatedAt
                               END
                      ELSE NULL
                   END AS modifiedAt
                   FROM PartitionUser pu
                   WHERE pu.partition.tenant = :tenant
                      AND pu.user = :user
                      AND pu.partition.partitionPath LIKE CONCAT(:folder, '%')
                      AND LENGTH(pu.partition.partitionPath) > LENGTH(:folder)
                      AND pu.partition.removedAt IS NULL
                   """,
            countQuery = """
                         SELECT COUNT(DISTINCT CONCAT(
                           CASE
                              WHEN LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) = 0
                                  THEN SUBSTRING(pu.partition.partitionPath, LENGTH(:folder) + 1)
                              ELSE SUBSTRING(pu.partition.partitionPath, LENGTH(:folder) + 1, LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                           END, '|',
                           CASE
                              WHEN LOCATE('/', pu.partition.partitionPath, LENGTH(:folder) + 1) = 0
                                  THEN 'p'
                              ELSE 'f'
                           END)
                           )
                           FROM PartitionUser pu
                           WHERE pu.partition.tenant = :tenant
                              AND pu.user = :user
                              AND pu.partition.partitionPath LIKE CONCAT(:folder, '%')
                              AND LENGTH(pu.partition.partitionPath) > LENGTH(:folder)
                              AND pu.partition.removedAt IS NULL
                         """)
    Page<FileItem> findItems(@Param("tenant")Tenant tenant, @Param("folder") String folder, @Param("user") User user, Pageable pageable);
}
