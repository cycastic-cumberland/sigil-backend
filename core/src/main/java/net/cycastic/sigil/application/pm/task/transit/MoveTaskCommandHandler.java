package net.cycastic.sigil.application.pm.task.transit;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoveTaskCommandHandler implements Command.Handler<MoveTaskCommand, Void> {
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskStatusRepository taskStatusRepository;

    @Override
    public Void handle(MoveTaskCommand command) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        var status = taskStatusRepository.findByIdAndKanbanBoard_Id(command.getStatusId(), task.getKanbanBoard().getId())
                .orElseThrow(() -> new RequestException(404, "Task status not found"));
        task.setTaskStatus(status);
        taskRepository.save(task);

        return null;
    }
}
