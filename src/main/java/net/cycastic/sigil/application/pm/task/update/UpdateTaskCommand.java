package net.cycastic.sigil.application.pm.task.update;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@RetryOnStale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class UpdateTaskCommand implements Command<Void> {
    @NotNull
    private String taskId;

    @Nullable
    private TaskPriority taskPriority;

    private String encryptedName;

    private String encryptedContent;

    private String iv;
}
