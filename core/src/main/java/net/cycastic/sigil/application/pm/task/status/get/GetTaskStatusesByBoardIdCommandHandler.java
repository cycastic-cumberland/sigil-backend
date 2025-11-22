package net.cycastic.sigil.application.pm.task.status.get;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.application.pm.task.status.TaskStatusService;
import net.cycastic.sigil.domain.dto.pm.TaskProgressDto;
import net.cycastic.sigil.domain.dto.pm.TaskStatusDto;
import net.cycastic.sigil.domain.dto.pm.TaskStatusesDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetTaskStatusesByBoardIdCommandHandler extends BaseProjectCommandHandler<GetTaskStatusesByBoardIdCommand, TaskStatusesDto> {
    private final TaskStatusRepository taskStatusRepository;
    private final KanbanBoardRepository kanbanBoardRepository;
    private final TaskStatusService taskStatusService;

    @Override
    protected TaskStatusesDto handleInternal(GetTaskStatusesByBoardIdCommand command, ProjectPartition projectPartition) {
        var board = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var statuses = taskStatusRepository.findByKanbanBoard_Id(board.getId(), Sort.by("id").ascending());

        return new TaskStatusesDto(taskStatusService.toDto(statuses));
    }
}
