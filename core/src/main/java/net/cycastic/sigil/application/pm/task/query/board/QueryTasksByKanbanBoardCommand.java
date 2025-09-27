package net.cycastic.sigil.application.pm.task.query.board;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.Data;
import net.cycastic.sigil.domain.dto.pm.TaskCardsDto;

@Data
public class QueryTasksByKanbanBoardCommand implements Command<TaskCardsDto> {
    @Min(1)
    private int kanbanBoardId;
}
