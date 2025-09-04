package net.cycastic.sigil.application.pm.task.update;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

@Data
@RetryOnStale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class UpdateTaskCommand implements Command<Void> {
    @NotEmpty
    private String taskId;

    @NotEmpty
    private String encryptedName;

    @NotEmpty
    private String encryptedContent;

    @NotEmpty
    private String iv;
}
