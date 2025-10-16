package net.cycastic.sigil.application.pm.task.comment.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.CommentDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.pm.TaskCommentRepository;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTaskCommentCommandHandler implements Command.Handler<QueryTaskCommentCommand, PageResponseDto<CommentDto>> {
    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PageResponseDto<CommentDto> handle(QueryTaskCommentCommand command) {
        var task = taskRepository.findByTenant_IdAndTaskIdentifier(loggedUserAccessor.getTenantId(), command.getTaskId())
                .orElseThrow(() -> new RequestException(404, "Task not found"));
        var comments = taskCommentRepository.findByTask(task, command.toPageable());
        return PageResponseDto.fromDomain(comments, CommentDto::fromDomain);
    }
}
