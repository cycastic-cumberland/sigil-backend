package net.cycastic.sigil.application.pm.task.comment;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.application.pm.task.SubscribersDiff;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskComment;
import net.cycastic.sigil.domain.repository.CipherRepository;
import net.cycastic.sigil.domain.repository.pm.TaskCommentRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskSubscriberRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveTaskCommentCommandHandler extends BaseProjectCommandHandler<SaveTaskCommentCommand, IdDto> {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CipherRepository cipherRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskSubscriberRepository taskSubscriberRepository;

    @Override
    protected IdDto handleInternal(SaveTaskCommentCommand command, ProjectPartition projectPartition) {
        var sender = userService.getUser();
        if (command.getId() == null){
            var task = taskRepository.findByProjectPartitionAndTaskIdentifier(projectPartition, command.getTaskId())
                    .orElseThrow(() -> new RequestException(404, "Task not found"));
            var subscriberDiff = new SubscribersDiff(taskSubscriberRepository, task, sender);
            var cipher = command.getEncryptedContent().toDomain();
            cipherRepository.save(cipher);
            var comment = TaskComment.builder()
                    .task(task)
                    .sender(sender)
                    .encryptedContent(cipher)
                    .build();
            taskCommentRepository.save(comment);
            subscriberDiff.apply();
        } else {
            var comment = taskCommentRepository.findByProjectPartitionAndId(projectPartition, command.getId())
                    .orElseThrow(() -> new RequestException(404, "Comment not found"));
            var subscriberDiff = new SubscribersDiff(taskSubscriberRepository, comment.getTask(), sender);
            var cipher = command.getEncryptedContent().toDomain();
            if (comment.getEncryptedContent().copyFrom(cipher)){
                cipherRepository.save(comment.getEncryptedContent());
            }
            subscriberDiff.apply();
        }

        return null;
    }
}
