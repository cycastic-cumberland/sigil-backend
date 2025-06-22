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

    @Query("SELECT DISTINCT " +
            "CASE " +
            "   WHEN LOCATE('/', l.listingPath, BYTELENGTH(:folder) + 1) = 0 " +
            "       THEN SUBSTRING(l.listingPath, BYTELENGTH(:folder) + 1) " +
            "   ELSE SUBSTRING(l.listingPath, BYTELENGTH(:folder) + 1, LOCATE('/', l.listingPath, BYTELENGTH(:folder) + 1) - BYTELENGTH(:folder) - 1) " +
            "END AS listingPath, " +
            "CASE " +
            "   WHEN LOCATE('/', l.listingPath, BYTELENGTH(:folder) + 1) = 0 " +
            "       THEN l.type " +
            "   ELSE NULL " +
            "END AS type " +
            "FROM Listing l " +
            "WHERE l.project = :project " +
            "   AND l.listingPath LIKE CONCAT(:folder, '%') " +
            "   AND BYTELENGTH(l.listingPath) > BYTELENGTH(:folder) " +
            "   AND l.removedAt IS NULL")
    List<FileItem> findItems(@Param("project") Project project, @Param("folder") String folder);
}
