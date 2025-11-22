package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.TaskStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    List<TaskStatus> findByKanbanBoard_Id(int kanbanBoardId, Sort sort);
    Optional<TaskStatus> findByIdAndKanbanBoard_Id(long id, int kanbanBoardId);
    List<TaskStatus> findByKanbanBoard_IdAndIdIn(int kanbanBoardId, Collection<Long> ids);
}
