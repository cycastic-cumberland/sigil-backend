package net.cycastic.sigil.application.pm.task.create;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionChecksum;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@RetryOnStale // Latest task ID might stale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class CreateTaskCommand implements Command<IdDto>, PartitionChecksum {
    @Nullable
    private Integer kanbanBoardId;

    @NotBlank
    @Base64String
    private String partitionChecksum;

    @Nullable
    private Long taskStatusId;

    @Nullable
    private TaskPriority taskPriority;

    @NotNull
    private CipherDto encryptedName;

    @Nullable
    private CipherDto encryptedContent;
}
