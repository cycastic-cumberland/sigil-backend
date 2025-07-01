package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Integer> {
    interface FileItem {
        String getName();
        ListingType getType();
        OffsetDateTime getModifiedAt();
        Boolean getAttachmentUploadCompleted();
    }

    void removeByTypeAndAttachmentListing(@NotNull ListingType type, AttachmentListing attachmentListing);

    Page<Listing> findListingsByProjectAndListingPathStartingWith(@NotNull Project project, @NotNull String path, Pageable pageable);

    Optional<Listing> findByProjectAndListingPath(@NotNull Project project, @NotNull String listingPath);

    Optional<Listing> findByProject_IdAndListingPath(int projectId, @NotNull String listingPath);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.project.id = :projectId AND l.listingPath = :listingPath")
    Optional<Listing> findByProject_IdAndListingPathForUpdate(@Param("projectId") int projectId, @Param("listingPath") @NotNull String listingPath);

    Listing findByAttachmentListing(@NotNull AttachmentListing attachmentListing);

    @Query(value = """
                   SELECT DISTINCT
                   CASE
                      WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0
                          THEN SUBSTRING(l.listingPath, LENGTH(:folder) + 1)
                      ELSE SUBSTRING(l.listingPath, LENGTH(:folder) + 1, LOCATE('/', l.listingPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                   END AS name,
                   CASE
                      WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0
                          THEN l.type
                      ELSE NULL
                   END AS type,
                   CASE
                      WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0
                          THEN CASE
                                  WHEN l.updatedAt IS NULL THEN l.createdAt
                                  ELSE l.updatedAt
                               END
                      ELSE NULL
                   END AS modifiedAt,
                   CASE
                      WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0 THEN CASE
                        WHEN l.attachmentListing IS NOT NULL THEN l.attachmentListing.uploadCompleted
                        ELSE NULL END
                      ELSE NULL END AS attachmentUploadCompleted
                   FROM Listing l
                   WHERE l.project = :project
                      AND l.listingPath LIKE CONCAT(:folder, '%')
                      AND LENGTH(l.listingPath) > LENGTH(:folder)
                      AND l.removedAt IS NULL
                   """,
            countQuery = """
                         SELECT COUNT(DISTINCT 
                             CONCAT(
                                 CASE
                                     WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0
                                         THEN SUBSTRING(l.listingPath, LENGTH(:folder) + 1)
                                     ELSE SUBSTRING(l.listingPath, LENGTH(:folder) + 1, LOCATE('/', l.listingPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1)
                                 END,
                                 '|',
                                 COALESCE(
                                     CASE
                                         WHEN LOCATE('/', l.listingPath, LENGTH(:folder) + 1) = 0
                                             THEN l.type
                                         ELSE NULL
                                     END, 'NULL'
                                 )
                             )
                         )
                         FROM Listing l
                         WHERE l.project = :project
                             AND l.listingPath LIKE CONCAT(:folder, '%')
                             AND LENGTH(l.listingPath) > LENGTH(:folder)
                             AND l.removedAt IS NULL
                        """
    )
    Page<FileItem> findItems(@Param("project") Project project, @Param("folder") String folder, Pageable pageable);
}
