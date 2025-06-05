package net.cycastic.portfoliotoolkit.domain.repository;

import net.cycastic.portfoliotoolkit.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
