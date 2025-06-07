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
    @Query(
            value = "SELECT DISTINCT " +
                    "CASE " +
                    "  WHEN LOCATE(UNHEX('2A0000'), SUBSTRING(search_key, LENGTH(:prefix) + 4)) > 0 " +
                    "  THEN SUBSTRING( " +
                    "         SUBSTRING(search_key, LENGTH(:prefix) + 4), " +
                    "         1, " +
                    "         LOCATE(UNHEX('2A0000'), SUBSTRING(search_key, LENGTH(:prefix) + 4)) - 1 " +
                    "       ) " +
                    "  ELSE SUBSTRING(search_key, LENGTH(:prefix) + 4) " +
                    "END AS folder " +
                    "FROM listings " +
                    "WHERE project_id = :projectId AND search_key LIKE CONCAT(:prefix, UNHEX('2A0000'), '%')",
            nativeQuery = true
    )
    List<byte[]> findDistinctFolders(@Param("projectId") int projectId, @Param("prefix") byte[] prefix);

    Page<Listing> findListingsByProjectAndSearchKeyGreaterThanEqualAndSearchKeyLessThan(@NotNull Project project, @NotNull byte[] searchKeyIsGreaterThan, @NotNull byte[] searchKeyIsLessThan, Pageable pageable);
}
