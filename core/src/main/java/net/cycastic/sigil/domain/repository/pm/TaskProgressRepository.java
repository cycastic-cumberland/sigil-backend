package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.KanbanBoard;
import net.cycastic.sigil.domain.model.pm.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {
    List<TaskProgress> findByFromStatus_KanbanBoardAndNextStatus_KanbanBoard(KanbanBoard fromStatusKanbanBoard, KanbanBoard nextStatusKanbanBoard);
}
