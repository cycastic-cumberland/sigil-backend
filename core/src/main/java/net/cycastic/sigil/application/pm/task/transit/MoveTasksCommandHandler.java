package net.cycastic.sigil.application.pm.task.transit;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.application.pm.task.status.TaskStatusService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStereotype;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.domain.repository.pm.TaskUniqueStatusRepository;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MoveTasksCommandHandler extends BaseProjectCommandHandler<MoveTasksCommand, Void> {
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusService taskStatusService;
    private final TaskUniqueStatusRepository taskUniqueStatusRepository;
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected Void handleInternal(MoveTasksCommand command, ProjectPartition projectPartition) {
        var kanbanBoard = kanbanBoardRepository.findByIdAndProjectPartition_Id(command.getKanbanBoardId(), projectPartition.getId())
                .orElseThrow(() -> new RequestException(404, "Board not found"));
        var tasks = taskRepository.findByKanbanBoardAndTaskIdentifierIn(kanbanBoard, command.getTasks().keySet());
        if (tasks.size() != command.getTasks().size()){
            throw new RequestException(404, "Some tasks were not found");
        }

        var statusMap = taskStatusRepository.findByKanbanBoard_IdAndIdIn(command.getKanbanBoardId(), command.getTasks().values()).stream()
                .collect(Collectors.toMap(TaskStatus::getId, t -> t));
        if (statusMap.size() != command.getTasks().size()){
            throw new RequestException(404, "Some task statuses were not found");
        }

        var backlogStereotype = taskUniqueStatusRepository.findByKanbanBoardAndTaskUniqueStereotype(kanbanBoard, TaskUniqueStereotype.BACKLOG);

        var pair = taskStatusService.toMappingPair(statusMap.values());
        for (var task : tasks){
            TaskStatus currentStatus;
            if (task.getTaskStatus() == null){
                if (backlogStereotype.isEmpty()){
                    throw new RequestException(400, task.getTaskIdentifier() + " can not be transited from backlog status");
                }

                currentStatus = backlogStereotype.get().getTaskStatus();
            } else {
                currentStatus = task.getTaskStatus();
            }

            var desiredStatusId = command.getTasks().get(task.getTaskIdentifier());
            if (currentStatus.getId().equals(desiredStatusId)){
                continue;
            }
            var eligibleNextStatus = pair.fromToMap().get(currentStatus.getId());
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
