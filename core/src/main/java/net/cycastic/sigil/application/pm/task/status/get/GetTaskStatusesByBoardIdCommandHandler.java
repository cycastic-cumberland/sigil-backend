package net.cycastic.sigil.application.pm.task.status.get;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
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
    private final TaskProgressRepository taskProgressRepository;

    @Override
    protected TaskStatusesDto handleInternal(GetTaskStatusesByBoardIdCommand command, ProjectPartition projectPartition) {
        var board = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var statuses = taskStatusRepository.findByKanbanBoard_Id(board.getId(), Sort.by("id").ascending());
        var progress = taskProgressRepository.findByFromStatus_KanbanBoardAndNextStatus_KanbanBoard(board, board);
        var fromToMap = progress.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getFromStatus().getId(),
                        Collectors.mapping(TaskProgressDto::fromDomain, Collectors.toList())));
        var toFromMap = progress.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getNextStatus().getId(),
                        Collectors.mapping(TaskProgressDto::fromDomain, Collectors.toList())));

        var statusDtos = new ArrayList<TaskStatusDto>(statuses.size());
        for (var status : statuses) {
            var dto = TaskStatusDto.fromDomain(status);
            var fromIds = toFromMap.get(dto.getId());
            dto.setPreviousTaskStatuses(fromIds);
            var toIds = fromToMap.get(dto.getId());
            dto.setNextTaskStatuses(toIds);

            statusDtos.add(dto);
        }
        return new TaskStatusesDto(statusDtos);
    }
}
