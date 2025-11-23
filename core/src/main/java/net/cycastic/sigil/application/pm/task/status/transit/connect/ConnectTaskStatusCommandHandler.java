package net.cycastic.sigil.application.pm.task.status.transit.connect;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskProgress;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConnectTaskStatusCommandHandler extends BaseProjectCommandHandler<ConnectTaskStatusCommand, Void> {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskProgressRepository taskProgressRepository;

    @Override
    protected Void handleInternal(ConnectTaskStatusCommand command, ProjectPartition projectPartition) {
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

        for (var connection : command.getConnections()){
            var progressOpt = taskProgressRepository.findByFromStatusAndNextStatus(statusMap.get(connection.getFromStatusId()),
                    statusMap.get(connection.getToStatusId()));
            TaskProgress progress;
            if (progressOpt.isPresent()){
                progress = progressOpt.get();
                if (progress.getProgressionName().equals(connection.getStatusName())){
                    continue;
                }
                progress.setProgressionName(connection.getStatusName());
            } else {
                progress = TaskProgress.builder()
                        .fromStatus(statusMap.get(connection.getFromStatusId()))
                        .nextStatus(statusMap.get(connection.getToStatusId()))
                        .progressionName(connection.getStatusName())
                        .build();
            }
            taskProgressRepository.save(progress);
        }
        return null;
    }
}
