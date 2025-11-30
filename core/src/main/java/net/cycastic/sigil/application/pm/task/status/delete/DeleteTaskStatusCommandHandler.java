package net.cycastic.sigil.application.pm.task.status.delete;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteTaskStatusCommandHandler extends BaseProjectCommandHandler<DeleteTaskStatusCommand, Void> {
    private final TaskStatusRepository taskStatusRepository;
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected Void handleInternal(DeleteTaskStatusCommand command, ProjectPartition projectPartition) {
        var ids = command.getDeserializedStatusIds();
        var kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var statuses = taskStatusRepository.findByIdInAndKanbanBoard(ids, kanbanBoard);
        if (statuses.size() != ids.size()){
            throw new RequestException(404, "Some statuses were not found");
        }
        taskStatusRepository.deleteAll(statuses);
        return null;
    }
}
