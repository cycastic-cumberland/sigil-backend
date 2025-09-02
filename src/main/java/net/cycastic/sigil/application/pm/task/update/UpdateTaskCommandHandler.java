package net.cycastic.sigil.application.pm.task.update;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class UpdateTaskCommandHandler extends BaseProjectCommandHandler<UpdateTaskCommand, Void> {
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    protected Void handleInternal(UpdateTaskCommand command, ProjectPartition projectPartition) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        task.setEncryptedName(Base64.getDecoder().decode(command.getEncryptedName()));
        task.setEncryptedContent(Base64.getDecoder().decode(command.getEncryptedContent()));
        task.setIv(Base64.getDecoder().decode(command.getIv()));
        taskRepository.save(task);

        return null;
    }
}
