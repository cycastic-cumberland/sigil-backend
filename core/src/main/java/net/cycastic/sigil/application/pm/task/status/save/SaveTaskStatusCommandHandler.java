package net.cycastic.sigil.application.pm.task.status.save;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStatus;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStereotype;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.domain.repository.pm.TaskUniqueStatusRepository;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SaveTaskStatusCommandHandler extends BaseProjectCommandHandler<SaveTaskStatusCommand, IdDto> {
    private final TaskStatusRepository taskStatusRepository;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final TaskUniqueStatusRepository taskUniqueStatusRepository;

    private TaskUniqueStatus assignStereotype(TaskStatus taskStatus, TaskUniqueStereotype taskUniqueStereotype){
        Supplier<TaskUniqueStatus> createNewUniqueStatus = () -> TaskUniqueStatus.builder()
                .taskStatus(taskStatus)
                .kanbanBoard(taskStatus.getKanbanBoard())
                .taskUniqueStereotype(taskUniqueStereotype)
                .build();

        // Newly created status
        if (taskStatus.getId() == null){
            return createNewUniqueStatus.get();
        }
        var opt = taskUniqueStatusRepository.findById(taskStatus.getId());
        if (opt.isPresent()){
            var unique = opt.get();
            unique.setTaskUniqueStereotype(taskUniqueStereotype);
            return unique;
        } else {
            return createNewUniqueStatus.get();
        }
    }

    @Override
    protected IdDto handleInternal(SaveTaskStatusCommand command, ProjectPartition projectPartition) {
        TaskStatus taskStatus;
        if (command.getId() != null){
            taskStatus = taskStatusRepository.findById(command.getId())
                    .orElseThrow(() -> new RequestException(404, "Status not found"));
        } else {
            taskStatus = new TaskStatus();
            var kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                    .orElseThrow(() -> new RequestException(404, "Kanban board not found"));
            taskStatus.setKanbanBoard(kanbanBoard);
        }

        taskStatus.setStatusName(command.getStatusName());
        taskStatusRepository.save(taskStatus);
        if (command.getStereotype() != null){
            var uniqueStatus = assignStereotype(taskStatus, command.getStereotype());
            taskUniqueStatusRepository.save(uniqueStatus);
        } else if (taskStatus.getTaskUniqueStatus() != null) {
            taskStatus.setTaskUniqueStatus(null);
            taskStatusRepository.save(taskStatus);
        }
        return new IdDto(taskStatus.getId());
    }
}
