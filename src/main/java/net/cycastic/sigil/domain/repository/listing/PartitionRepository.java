package net.cycastic.sigil.domain.repository.listing;

import net.cycastic.sigil.domain.model.Partition;
import net.cycastic.sigil.domain.model.Tenant;
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
        boolean getIsFolder();
        OffsetDateTime getModifiedAt();
    }

    Optional<Partition> findByTenant_IdAndPartitionPath(int tenantId, String partitionPath);

    @Query(value = """
                   SELECT DISTINCT
                   CASE
                      WHEN LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN SUBSTRING(p.partitionPath, LENGTH(:folder) + 1)
                      ELSE SUBSTRING(p.partitionPath, LENGTH(:folder) + 1, LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                   END AS name,
                   CASE
                      WHEN LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN FALSE
                      ELSE TRUE
                   END AS isFolder,
                   CASE
                      WHEN LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) = 0
                          THEN CASE
                                  WHEN p.updatedAt IS NULL THEN p.createdAt
                                  ELSE p.updatedAt
                               END
                      ELSE NULL
                   END AS modifiedAt
                   FROM Partition p
                   WHERE p.tenant = :tenant
                      AND p.partitionPath LIKE CONCAT(:folder, '%')
                      AND LENGTH(p.partitionPath) > LENGTH(:folder)
                      AND p.removedAt IS NULL
                   """,
            countQuery = """
                         SELECT COUNT(DISTINCT CONCAT(CASE
                              WHEN LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) = 0
                                  THEN SUBSTRING(p.partitionPath, LENGTH(:folder) + 1)
                              ELSE SUBSTRING(p.partitionPath, LENGTH(:folder) + 1, LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                           END,
                           '|',
                           CASE
                              WHEN LOCATE('/', p.partitionPath, LENGTH(:folder) + 1) = 0
                                  THEN 'f'
                              ELSE 't'
                           END)
                           )
                           FROM Partition p
                           WHERE p.tenant = :tenant
                              AND p.partitionPath LIKE CONCAT(:folder, '%')
                              AND LENGTH(p.partitionPath) > LENGTH(:folder)
                              AND p.removedAt IS NULL
                         """)
    Page<FileItem> findItems(@Param("tenant")Tenant tenant, @Param("folder") String folder, Pageable pageable);
}
