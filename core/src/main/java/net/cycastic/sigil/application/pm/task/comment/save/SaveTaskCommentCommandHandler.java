package net.cycastic.sigil.application.pm.task.comment.save;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.application.pm.task.SubscribersDiff;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskComment;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.domain.repository.pm.TaskCommentRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskSubscriberRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SaveTaskCommentCommandHandler implements Command.Handler<SaveTaskCommentCommand, IdDto> {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CipherRepository cipherRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskSubscriberRepository taskSubscriberRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PartitionRepository partitionRepository;
    private final PartitionService partitionService;

    private void verifyPartitionWriteAccess(Task task, String checksumBase64){
        var checksum = Base64.getDecoder().decode(checksumBase64);
        var partition = partitionRepository.findByTask(task);
        if (!CryptographicUtilities.constantTimeEquals(checksum, partition.getKeySha256Digest())){
            throw RequestException.withExceptionCode("C400T006");
        }

        partitionService.checkPermission(partition.getId(), ApplicationConstants.PartitionPermissions.WRITE);
    }

    @Override
    public IdDto handle(SaveTaskCommentCommand command) {
        var currentUser = userService.getUser();
        TaskComment comment;
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        verifyPartitionWriteAccess(task, command.getPartitionChecksum());
        if (command.getId() == null){
            var subscriberDiff = new SubscribersDiff(taskSubscriberRepository, task, currentUser);
            var cipher = command.getEncryptedContent().toDomain();
            cipherRepository.save(cipher);
            comment = TaskComment.builder()
                    .task(task)
                    .sender(currentUser)
                    .encryptedContent(cipher)
                    .build();
            subscriberDiff.apply();
        } else {
            comment = taskCommentRepository.findByTaskAndId(task, command.getId())
                    .orElseThrow(() -> new RequestException(404, "Comment not found"));
            if (!Objects.equals(comment.getTask().getTaskIdentifier(), command.getTaskId())){
                throw new RequestException(400, "Mismatched task identifier");
            }
            if (!comment.getSender().getId().equals(currentUser.getId()) &&
                    !currentUser.getAuthorities().contains(User.Authorities.ADMIN)){
                throw RequestException.forbidden();
            }
            var subscriberDiff = new SubscribersDiff(taskSubscriberRepository, comment.getTask(), currentUser);
            var cipher = command.getEncryptedContent().toDomain();
            if (comment.getEncryptedContent().copyFrom(cipher)){
                cipherRepository.save(comment.getEncryptedContent());
            }
            subscriberDiff.apply();
            // Manually trigger update
            comment.setUpdatedAt(OffsetDateTime.now());
        }

        taskCommentRepository.save(comment);
        return new IdDto(comment.getId());
    }
}
