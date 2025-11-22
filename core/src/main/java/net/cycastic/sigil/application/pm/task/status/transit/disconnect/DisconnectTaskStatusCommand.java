package net.cycastic.sigil.application.pm.task.status.transit.disconnect;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class DisconnectTaskStatusCommand implements Command<Void> {
    @Min(1)
    private long fromStatusId;

    @Min(1)
    private long toStatusId;
}
