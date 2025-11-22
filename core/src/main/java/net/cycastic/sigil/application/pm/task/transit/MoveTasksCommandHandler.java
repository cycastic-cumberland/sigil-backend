package net.cycastic.sigil.application.pm.task.transit;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.task.status.TaskStatusService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MoveTasksCommandHandler implements Command.Handler<MoveTasksCommand, Void> {
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusService taskStatusService;

    @Override
    public Void handle(MoveTasksCommand command) {
        var tasks = taskRepository.findByTenant_IdAndTaskIdentifierIn(loggedUserAccessor.getTenantId(), command.getTasks().keySet());
        if (tasks.size() != command.getTasks().size()){
            throw new RequestException(404, "Some tasks were not found");
        }

        var statusMap = taskStatusRepository.findByKanbanBoard_IdAndIdIn(command.getKanbanBoardId(), command.getTasks().values()).stream()
                .collect(Collectors.toMap(TaskStatus::getId, t -> t));
        if (statusMap.size() != command.getTasks().size()){
            throw new RequestException(404, "Some task statuses were not found");
        }

        var pair = taskStatusService.toMappingPair(statusMap.values());
        for (var task : tasks){
            var desiredStatusId = command.getTasks().get(task.getTaskIdentifier());
            var eligibleNextStatus = pair.fromToMap().get(task.getTaskStatus().getId());
            if (eligibleNextStatus != null){
                var optNext = eligibleNextStatus.stream()
                        .filter(t -> t.getToStatusId() == desiredStatusId)
                        .findFirst();
                if (optNext.isPresent()) {
                    var desiredStatus = statusMap.get(optNext.get().getToStatusId());
                    if (desiredStatus == null){
                        throw new RequestException(404, "Task status not found");
                    }

                    task.setTaskStatus(desiredStatus);
                    taskRepository.save(task);
                    continue;
                }
            }
            var eligiblePrevStatus = pair.toFromMap().get(task.getTaskStatus().getId());
            if (eligiblePrevStatus != null) {
                var optPrev = eligiblePrevStatus.stream()
                        .filter(t -> t.getFromStatusId() == desiredStatusId)
                        .findFirst();
                if (optPrev.isPresent()){
                    var desiredStatus = statusMap.get(optPrev.get().getFromStatusId());
                    if (desiredStatus == null){
                        throw new RequestException(404, "Task status not found");
                    }

                    task.setTaskStatus(desiredStatus);
                    taskRepository.save(task);
                    continue;
                }
            }

            throw new RequestException(404, "Task status not found");
        }

        return null;
    }
}
