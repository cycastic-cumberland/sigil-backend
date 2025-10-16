package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    long countByTask(Task task);

    Page<TaskComment> findByTask(Task task, Pageable pageable);

    Optional<TaskComment> findByTaskAndId(Task task, long id);

    int deleteByIdAndTask(long id, Task task);
}
