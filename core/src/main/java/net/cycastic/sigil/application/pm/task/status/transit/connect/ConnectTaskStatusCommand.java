package net.cycastic.sigil.application.pm.task.status.transit.connect;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TransactionalCommand
@Retry(value = {Retry.Event.STALE, Retry.Event.INTEGRITY_VIOLATION})
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class ConnectTaskStatusCommand implements Command<Void> {
    @Min(1)
    private int kanbanBoardId;

    @Min(1)
    private long fromStatusId;

    @Min(1)
    private long toStatusId;

    @NotEmpty
    private String statusName;
}
