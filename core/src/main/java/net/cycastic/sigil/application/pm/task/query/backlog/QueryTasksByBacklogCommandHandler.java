package net.cycastic.sigil.application.pm.task.query.backlog;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.TaskDto;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTasksByBacklogCommandHandler implements Command.Handler<QueryTasksByBacklogCommand, PageResponseDto<TaskDto>> {
    private final TaskRepository taskRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public PageResponseDto<TaskDto> handle(QueryTasksByBacklogCommand command) {
        // TODO: Backlog unique task status union
        var page = taskRepository.findByTenant_IdAndTaskStatus_Id(loggedUserAccessor.getTenantId(),
                null,
                command.toPageable());
        return PageResponseDto.fromDomain(page, TaskDto::fromDomain);
    }
}
