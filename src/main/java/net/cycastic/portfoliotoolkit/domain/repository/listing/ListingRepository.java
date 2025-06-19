package net.cycastic.portfoliotoolkit.domain.repository.listing;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Integer> {
    Page<Listing> findListingsByProjectAndListingPathStartingWith(@NotNull Project project, @NotNull String path, Pageable pageable);

    @Query("""
            SELECT DISTINCT
             CASE
               WHEN :folder = '/' THEN
                 CASE
                   WHEN LOCATE('/', e.listingPath, 2) > 0
                     THEN SUBSTRING(e.listingPath, 2, LOCATE('/', e.listingPath, 2) - 2)
                   ELSE SUBSTRING(e.listingPath, 2)
                 END
               ELSE
                 CASE
                   WHEN LOCATE('/', e.listingPath, LENGTH(:folder) + 2) > 0
                     THEN SUBSTRING(
                            e.listingPath,
                            LENGTH(:folder) + 2,
                            LOCATE('/', e.listingPath, LENGTH(:folder) + 2) - LENGTH(:folder) - 2
                          )
                   ELSE SUBSTRING(e.listingPath, LENGTH(:folder) + 2)
                 END
             END
           FROM Listing e
           WHERE e.project = :project AND
             (
               :folder = '/'
               AND e.listingPath LIKE '/%'
               AND e.listingPath <> '/'
             )
             OR
             (
               :folder <> '/'
               AND e.listingPath LIKE CONCAT(:folder, '/%')
               AND e.listingPath <> :folder
             )
           """)
    List<String> findSubfolders(@Param("project") Project project, @Param("folder") String folder);
}
