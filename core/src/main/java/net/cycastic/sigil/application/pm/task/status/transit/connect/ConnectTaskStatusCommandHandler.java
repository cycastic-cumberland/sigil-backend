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

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConnectTaskStatusCommandHandler extends BaseProjectCommandHandler<ConnectTaskStatusCommand, Void> {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskProgressRepository taskProgressRepository;

    @Override
    protected Void handleInternal(ConnectTaskStatusCommand command, ProjectPartition projectPartition) {
        var statuses = taskStatusRepository.findByKanbanBoard_IdAndIdIn(command.getKanbanBoardId(), List.of(
                command.getFromStatusId(), command.getToStatusId()
        ));

        if (statuses.size() != 2){
            throw new RequestException(404, "Some statuses could not be found");
        }

        var statusMap = statuses.stream()
                .collect(Collectors.toMap(TaskStatus::getId, t -> t));
        var progressOpt = taskProgressRepository.findByFromStatusAndNextStatus(statusMap.get(command.getFromStatusId()),
                statusMap.get(command.getToStatusId()));
        if (progressOpt.isPresent()){
            var progress = progressOpt.get();
            progress.setProgressionName(command.getStatusName());
            taskProgressRepository.save(progress);
        } else {
            var progress = TaskProgress.builder()
                    .fromStatus(statusMap.get(command.getFromStatusId()))
                    .nextStatus(statusMap.get(command.getToStatusId()))
                    .progressionName(command.getStatusName())
                    .build();
            taskProgressRepository.save(progress);
        }
        return null;
    }
}
