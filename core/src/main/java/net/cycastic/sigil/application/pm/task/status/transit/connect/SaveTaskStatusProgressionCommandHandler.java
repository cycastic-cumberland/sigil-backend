package net.cycastic.sigil.application.pm.task.status.transit.connect;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.SimpleDiffUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskProgress;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SaveTaskStatusProgressionCommandHandler extends BaseProjectCommandHandler<SaveTaskStatusProgressionCommand, Void> {
    @Data
    private static class FromToPair {
        private final Long fromId;
        private final Long toId;

        public FromToPair(TaskProgress taskProgress){
            fromId = taskProgress.getFromStatus().getId();
            toId = taskProgress.getNextStatus().getId();
        }
    }
    @SuperBuilder
    private static class DiffUtilities extends SimpleDiffUtilities<TaskProgress, FromToPair>{ }

    private static final DiffUtilities DIFF = DiffUtilities.builder()
            .keySelector(FromToPair::new)
            .comparator((a, b) -> a.getProgressionName().equals(b.getProgressionName()))
            .build();

    private final TaskStatusRepository taskStatusRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected Void handleInternal(SaveTaskStatusProgressionCommand command, ProjectPartition projectPartition) {
        var kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var statusIds = command.getConnections().stream()
                .map(c -> List.of(c.getFromStatusId(), c.getToStatusId()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        var statuses = taskStatusRepository.findByKanbanBoard_IdAndIdIn(command.getKanbanBoardId(), statusIds);

        if (statuses.size() != statusIds.size()){
            throw new RequestException(404, "Some statuses could not be found");
        }

        var statusMap = statuses.stream()
                .collect(Collectors.toMap(TaskStatus::getId, t -> t));

        var allProgresses = taskProgressRepository.findByFromStatus_KanbanBoard(kanbanBoard);

        var newProgresses = command.getConnections().stream()
                .map(c -> (TaskProgress)TaskProgress.builder()
                        .fromStatus(statusMap.get(c.getFromStatusId()))
                        .nextStatus(statusMap.get(c.getToStatusId()))
                        .progressionName(c.getStatusName())
                        .build())
                .toList();

        var diff = DIFF.shallowDiff(allProgresses, newProgresses);

        for (var updated : diff.getUpdatedEntities()){
            updated.original().setProgressionName(updated.updated().getProgressionName());
        }
        taskProgressRepository.saveAll(diff.getNewEntities());
        taskProgressRepository.saveAll(diff.getUpdatedEntities().stream()
                .map(SimpleDiffUtilities.UpdatedEntity::original)
                .toList());
        taskProgressRepository.deleteAll(diff.getDeletedEntities());

        return null;
    }
}
