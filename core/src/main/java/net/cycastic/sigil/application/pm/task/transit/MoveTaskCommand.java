package net.cycastic.sigil.application.pm.task.transit;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

@Data
@Retry(value = Retry.Event.STALE)
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class MoveTaskCommand implements Command<Void> {
    @NotNull
    private String taskId;

    @Nullable
    private Integer kanbanBoardId;

    @Min(1)
    private long statusId;
}
