package net.cycastic.sigil.application.pm.kanban.query.project;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryKanbanBoardByProjectCommandHandler implements Command.Handler<QueryKanbanBoardByProjectCommand, PageResponseDto<KanbanBoardDto>> {
    private final KanbanBoardRepository kanbanBoardRepository;

    @Override
    public PageResponseDto<KanbanBoardDto> handle(QueryKanbanBoardByProjectCommand command) {
        var page = kanbanBoardRepository.findByProjectPartition_Id(command.getPartitionId(), command.toPageable());
        return PageResponseDto.fromDomain(page, KanbanBoardDto::fromDomain);
    }
}
