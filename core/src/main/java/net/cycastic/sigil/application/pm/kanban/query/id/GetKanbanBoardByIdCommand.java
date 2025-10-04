package net.cycastic.sigil.application.pm.kanban.query.id;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.cycastic.sigil.domain.dto.pm.KanbanBoardDto;

@Data
@AllArgsConstructor
public class GetKanbanBoardByIdCommand implements Command<KanbanBoardDto> {
    @Min(1)
    private int id;
}
