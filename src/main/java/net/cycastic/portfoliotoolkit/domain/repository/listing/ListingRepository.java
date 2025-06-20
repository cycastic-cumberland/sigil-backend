package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Integer> {
    interface FileItem {
        String getListingPath();
        ListingType getType();
    }

    Page<Listing> findListingsByProjectAndListingPathStartingWith(@NotNull Project project, @NotNull String path, Pageable pageable);

    Optional<Listing> findByProjectAndListingPath(@NotNull Project project, @NotNull String listingPath);

    @Query(value =
            "SELECT DISTINCT " +
                    "  CASE " +
                    "    WHEN :folder = '/' THEN " +
                    "      CASE WHEN LOCATE('/', e.listingPath, 2) > 0 " +
                    "           THEN SUBSTRING(e.listingPath, 2, LOCATE('/', e.listingPath, 2) - 2) " +
                    "           ELSE SUBSTRING(e.listingPath, 2) " +
                    "      END " +
                    "    ELSE " +
                    "      CASE WHEN LOCATE('/', e.listingPath, LENGTH(:folder) + 1) > 0 " +
                    "           THEN SUBSTRING(e.listingPath, LENGTH(:folder) + 1, " +
                    "                         LOCATE('/', e.listingPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1) " +
                    "           ELSE SUBSTRING(e.listingPath, LENGTH(:folder) + 1) " +
                    "      END " +
                    "  END AS listingPath, " +
                    "  CASE " +
                    "    WHEN " +
                    "      (CASE " +
                    "         WHEN :folder = '/' THEN " +
                    "           CASE WHEN LOCATE('/', e.listingPath, 2) > 0 " +
                    "                THEN SUBSTRING(e.listingPath, 2, LOCATE('/', e.listingPath, 2) - 2) " +
                    "                ELSE SUBSTRING(e.listingPath, 2) " +
                    "           END " +
                    "         ELSE " +
                    "           CASE WHEN LOCATE('/', e.listingPath, LENGTH(:folder) + 1) > 0 " +
                    "                THEN SUBSTRING(e.listingPath, LENGTH(:folder) + 1, " +
                    "                              LOCATE('/', e.listingPath, LENGTH(:folder) + 1) - LENGTH(:folder) - 1) " +
                    "                ELSE SUBSTRING(e.listingPath, LENGTH(:folder) + 1) " +
                    "           END " +
                    "       END) LIKE '%.%' " +
                    "    THEN e.type " +
                    "    ELSE null " +
                    "  END AS type " +
                    "FROM Listing e " +
                    "WHERE e.project = :project AND (( " +
                    "         :folder = '/' " +
                    "         AND e.listingPath LIKE '/%' " +
                    "         AND e.listingPath <> '/' " +
                    "      ) " +
                    "   OR ( " +
                    "         :folder <> '/' " +
                    "         AND e.listingPath LIKE CONCAT(:folder, '%') " +
                    "         AND e.listingPath <> :folder " +
                    "      )) " +
                    "ORDER BY e.listingPath")
    List<FileItem> findItems(@Param("project") Project project, @Param("folder") String folder);

}
