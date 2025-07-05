package net.cycastic.sigil.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.model.Project;
import net.cycastic.sigil.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findProjectsByUser(@NotNull User user, Pageable pageable);

    @Query(
            value = "SELECT * FROM projects WHERE removed_at IS NOT NULL",
            countQuery = "SELECT COUNT(*) FROM projects WHERE removed_at IS NOT NULL",
            nativeQuery = true
    )
    Page<Project> findSoftDeletedProjects(Pageable pageable);

    int countByUser(@NotNull User user);
}
