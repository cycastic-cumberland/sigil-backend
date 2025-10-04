package net.cycastic.sigil.application.pm.task.query.backlog;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.TaskDto;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTasksByBacklogCommandHandler extends BaseProjectCommandHandler<QueryTasksByBacklogCommand, PageResponseDto<TaskDto>> {
    private final TaskRepository taskRepository;

    @Override
    protected PageResponseDto<TaskDto> handleInternal(QueryTasksByBacklogCommand command, ProjectPartition projectPartition) {
        // TODO: Backlog unique task status union
        var page = taskRepository.findByKanbanBoard_ProjectPartition_IdAndTaskStatus_Id(projectPartition.getId(),
                null,
                command.toPageable());
        return PageResponseDto.fromDomain(page, t -> TaskDto.fromDomain(t, projectPartition.getId()));
    }
}
