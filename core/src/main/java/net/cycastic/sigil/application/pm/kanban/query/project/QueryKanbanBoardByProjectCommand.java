package net.cycastic.sigil.application.pm.kanban.query.project;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryKanbanBoardByProjectCommand extends PageRequestDto implements Command<PageResponseDto<KanbanBoardDto>> {
}
