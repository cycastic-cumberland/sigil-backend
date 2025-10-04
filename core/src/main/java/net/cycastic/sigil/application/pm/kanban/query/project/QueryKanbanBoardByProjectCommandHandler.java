package net.cycastic.sigil.application.pm.kanban.query.project;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.pm.BaseProjectCommandHandler;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryKanbanBoardByProjectCommandHandler extends BaseProjectCommandHandler<QueryKanbanBoardByProjectCommand, PageResponseDto<KanbanBoardDto>> {
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    protected PageResponseDto<KanbanBoardDto> handleInternal(QueryKanbanBoardByProjectCommand command, ProjectPartition projectPartition) {
        var page = kanbanBoardRepository.findByProjectPartition_Id(projectPartition.getId(), command.toPageable());
        return PageResponseDto.fromDomain(page, KanbanBoardDto::fromDomain);
    }
}
