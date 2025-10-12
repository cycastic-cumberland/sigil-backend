package net.cycastic.sigil.application.pm.task.update;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionChecksum;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@Retry(value = {Retry.Event.STALE, Retry.Event.INTEGRITY_VIOLATION})
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class UpdateTaskCommand implements Command<Void>, PartitionChecksum {
    @NotNull
    private String taskId;

    @Nullable
    private Integer kanbanBoardId;

    @Nullable
    private String assigneeEmail;

    @Nullable
    private String reporterEmail;

    @NotNull
    private TaskPriority taskPriority;

    @NotNull
    private CipherDto encryptedName;

    @Nullable
    private CipherDto encryptedContent;

    @NotBlank
    @Base64String
    private String partitionChecksum;
}
