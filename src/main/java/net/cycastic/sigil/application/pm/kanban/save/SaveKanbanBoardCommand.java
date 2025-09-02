package net.cycastic.sigil.application.pm.kanban.save;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;
import net.cycastic.sigil.domain.dto.IdDto;

@Data
@TransactionalCommand
public class SaveKanbanBoardCommand implements Command<IdDto> {
    @Nullable
    private Integer id;

    @NotEmpty
    private String boardName;
}
