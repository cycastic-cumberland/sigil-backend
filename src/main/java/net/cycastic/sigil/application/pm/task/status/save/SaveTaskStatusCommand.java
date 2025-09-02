package net.cycastic.sigil.application.pm.task.status.save;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;
import net.cycastic.sigil.domain.dto.IdDto;

@Data
@TransactionalCommand
public class SaveTaskStatusCommand implements Command<IdDto> {
    @Nullable
    private Long id;

    @Nullable
    private Integer kanbanBoardId;

    @NotEmpty
    private String statusName;
}
