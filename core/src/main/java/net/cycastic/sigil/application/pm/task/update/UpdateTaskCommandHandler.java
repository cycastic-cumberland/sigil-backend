package net.cycastic.sigil.application.pm.task.update;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.application.pm.task.SubscribersDiff;
import net.cycastic.sigil.application.pm.task.transit.MoveTasksCommand;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskSubscriberRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateTaskCommandHandler extends BaseProjectCommandHandler<UpdateTaskCommand, Void> {
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PartitionUserRepository partitionUserRepository;
    private final TaskSubscriberRepository taskSubscriberRepository;
    private final CipherRepository cipherRepository;
    private final Pipelinr pipelinr;

    @Override
    protected Void handleInternal(UpdateTaskCommand command, ProjectPartition projectPartition) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        {
            var nameCipher = task.getEncryptedName();
            var newNameCipher = command.getEncryptedName().toDomain(true);
            if (nameCipher.copyFrom(newNameCipher)) {
                cipherRepository.save(nameCipher);
            }
        }

        var contentCipher = task.getEncryptedContent();
        if (command.getEncryptedContent() == null){
            if (contentCipher != null){
                task.setEncryptedContent(null);
                cipherRepository.delete(contentCipher);
            }
        } else {
            if (contentCipher == null){
                contentCipher = command.getEncryptedContent().toDomain(true);
                cipherRepository.save(contentCipher);
                task.setEncryptedContent(contentCipher);
            } else {
                var newContentCipher = command.getEncryptedContent().toDomain(true);
                if (contentCipher.copyFrom(newContentCipher)){
                    cipherRepository.save(contentCipher);
                }
            }
        }

        if ((task.getKanbanBoard() != null || command.getKanbanBoardId() != null) &&
                (task.getKanbanBoard() == null || task.getKanbanBoard().getId() == null ||
                        !task.getKanbanBoard().getId().equals(command.getKanbanBoardId()))) {
            throw new RequestException(500, "Unimplemented: Move board");
        }

        var subscribersDiff = new SubscribersDiff(taskSubscriberRepository, task);
        if (command.getAssigneeEmail() == null){
            task.setAssignee(null);
        } else if (!(task.getAssignee() != null && task.getAssignee().getEmail().equalsIgnoreCase(command.getAssigneeEmail()))){
            var assignee = partitionUserRepository.findPartitionMemberByEmail(projectPartition.getId(), command.getAssigneeEmail())
                    .orElseThrow(() -> new RequestException(404, "User not found: " + command.getAssigneeEmail()));
            task.setAssignee(assignee);
            subscribersDiff.subscribe(assignee);
        }
        if (command.getReporterEmail() == null){
            task.setReporter(null);
        } else if (!(task.getReporter() != null && task.getReporter().getEmail().equalsIgnoreCase(command.getReporterEmail()))){
            var reporter = partitionUserRepository.findPartitionMemberByEmail(projectPartition.getId(), command.getReporterEmail())
                    .orElseThrow(() -> new RequestException(404, "User not found: " + command.getReporterEmail()));
            task.setReporter(reporter);
            subscribersDiff.subscribe(reporter);
        }

        subscribersDiff.apply();
        task.setPriority(command.getTaskPriority());
        // Deliberately bump version
        task.setUpdatedAt(OffsetDateTime.now());
        taskRepository.save(task);

        var oldStatus = task.getTaskStatus();
        var newStatusId = command.getTaskStatusId();

        var unchanged = (oldStatus == null && newStatusId == null) ||
                        (oldStatus != null && oldStatus.getId().equals(newStatusId));

        if (task.getKanbanBoard() != null && !unchanged) {
            if (oldStatus != null && newStatusId == null) {
                throw new RequestException(400, "Task status ID cannot be null");
            }

            pipelinr.send(MoveTasksCommand.builder()
                            .tasks(Map.of(task.getTaskIdentifier(), newStatusId))
                            .kanbanBoardId(task.getKanbanBoard().getId())
                            .build());
        }

        return null;
    }
}
