package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    @Query("SELECT tc FROM TaskComment tc WHERE tc.id = :id AND tc.task.kanbanBoard.projectPartition = :projectPartition")
    Optional<TaskComment> findByProjectPartitionAndId(@Param("projectPartition") ProjectPartition projectPartition, @Param("id") Long id);
}
