package net.cycastic.sigil.application.pm.task.query.id;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.pm.TaskDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.pm.ProjectPartitionRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTaskByIdentifierCommandHandler implements Command.Handler<GetTaskByIdentifierCommand, TaskDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskRepository taskRepository;
    private final ProjectPartitionRepository projectPartitionRepository;

    @Override
    public TaskDto handle(GetTaskByIdentifierCommand command) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));

        var projectPartition = projectPartitionRepository.findByTask(task);
        return TaskDto.fromDomain(task, projectPartition.getId());
    }
}
