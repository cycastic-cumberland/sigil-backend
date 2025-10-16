package net.cycastic.sigil.application.pm.task.comment.count;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.CountDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.pm.TaskCommentRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountTaskCommentCommandHandler implements Command.Handler<CountTaskCommentCommand, CountDto> {
    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public CountDto handle(CountTaskCommentCommand command) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        var amount = taskCommentRepository.countByTask(task);
        return new CountDto(amount);
    }
}
