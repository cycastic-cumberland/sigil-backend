package net.cycastic.portfoliotoolkit.domain.repository;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.model.Project;
import net.cycastic.portfoliotoolkit.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findProjectsByUser(@NotNull User user, Pageable pageable);

    int countByUser(@NotNull User user);
}
