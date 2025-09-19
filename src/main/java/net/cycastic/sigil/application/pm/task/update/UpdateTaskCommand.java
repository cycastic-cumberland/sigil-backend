package net.cycastic.sigil.application.pm.task.update;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionChecksum;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@RetryOnStale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class UpdateTaskCommand implements Command<Void>, PartitionChecksum {
    @NotNull
    private String taskId;

    @Nullable
    private TaskPriority taskPriority;

    @Base64String
    private String encryptedName;

    @Base64String
    private String encryptedContent;

    @Base64String
    private String iv;

    @NotEmpty
    @Base64String
    private String partitionChecksum;
}
