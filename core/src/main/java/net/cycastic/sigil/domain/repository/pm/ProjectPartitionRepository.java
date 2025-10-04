package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectPartitionRepository extends JpaRepository<ProjectPartition, Integer> {
    @Query("SELECT t.kanbanBoard.projectPartition FROM Task t WHERE t = :task")
    ProjectPartition findByTask(@Param("task") Task task);
}
