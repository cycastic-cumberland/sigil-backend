package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.TaskStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    List<TaskStatus> findByKanbanBoard_Id(int kanbanBoardId, Sort sort);
}
