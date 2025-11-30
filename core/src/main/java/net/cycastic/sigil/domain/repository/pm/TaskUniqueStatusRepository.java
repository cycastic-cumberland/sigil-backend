package net.cycastic.sigil.domain.repository.pm;

import net.cycastic.sigil.domain.model.pm.KanbanBoard;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStatus;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStereotype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskUniqueStatusRepository extends JpaRepository<TaskUniqueStatus, Long> {
    Optional<TaskUniqueStatus> findByKanbanBoardAndTaskUniqueStereotype(KanbanBoard kanbanBoard, TaskUniqueStereotype taskUniqueStereotype);

    void deleteByKanbanBoardAndTaskUniqueStereotype(KanbanBoard kanbanBoard, TaskUniqueStereotype taskUniqueStereotype);
}
