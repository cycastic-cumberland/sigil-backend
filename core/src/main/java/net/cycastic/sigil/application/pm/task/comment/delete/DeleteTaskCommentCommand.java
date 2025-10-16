package net.cycastic.sigil.application.pm.task.comment.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@Data
@Retry(value = Retry.Event.STALE)
@TransactionalCommand
public class DeleteTaskCommentCommand implements Command<Void> {
    @Min(1)
    private long id;

    @NotEmpty
    private String taskId;
}
