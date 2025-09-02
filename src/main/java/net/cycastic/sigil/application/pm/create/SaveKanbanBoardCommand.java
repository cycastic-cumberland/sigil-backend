package net.cycastic.sigil.application.pm.create;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SaveKanbanBoardCommand implements Command<Void> {
    @Nullable
    private Integer id;

    @NotEmpty
    private String boardName;
}
