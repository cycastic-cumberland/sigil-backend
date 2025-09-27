package net.cycastic.sigil.application.pm.task.status.save;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveTaskStatusCommandHandler extends BaseProjectCommandHandler<SaveTaskStatusCommand, IdDto> {
    private final TaskStatusRepository taskStatusRepository;
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected IdDto handleInternal(SaveTaskStatusCommand command, ProjectPartition projectPartition) {
        TaskStatus taskStatus;
        if (command.getId() != null){
            taskStatus = taskStatusRepository.findById(command.getId())
                    .orElseThrow(() -> new RequestException(404, "Status not found"));
        } else {
            taskStatus = new TaskStatus();
            @SuppressWarnings("DataFlowIssue")
            var kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                    .orElseThrow(() -> new RequestException(404, "Kanban board not found"));
            taskStatus.setKanbanBoard(kanbanBoard);
        }

        taskStatus.setStatusName(command.getStatusName());
        taskStatusRepository.save(taskStatus);
        return new IdDto(taskStatus.getId());
    }
}
