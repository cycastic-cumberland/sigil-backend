package net.cycastic.sigil.application.pm.task.comment.delete;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.pm.TaskCommentRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteTaskCommentCommandHandler implements Command.Handler<DeleteTaskCommentCommand, Void> {
    private final PartitionRepository partitionRepository;
    private final PartitionService partitionService;
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskCommentRepository taskCommentRepository;

    private void verifyPartitionWriteAccess(Task task){
        var partition = partitionRepository.findByTask(task);
        partitionService.checkPermission(partition.getId(), ApplicationConstants.PartitionPermissions.WRITE);
    }

    @Override
    public Void handle(DeleteTaskCommentCommand command) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        verifyPartitionWriteAccess(task);
        var deleted = taskCommentRepository.deleteByIdAndTask(command.getId(), task);
        if (deleted <= 0){
            throw new RequestException(404, "Comment not found");
        }
        return null;
    }
}
