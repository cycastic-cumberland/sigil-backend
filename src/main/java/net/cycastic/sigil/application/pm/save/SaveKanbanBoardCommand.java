package net.cycastic.sigil.application.pm.save;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;

@Data
@TransactionalCommand
public class SaveKanbanBoardCommand implements Command<Void> {
    @Nullable
    private Integer id;

    @NotEmpty
    private String boardName;
}
