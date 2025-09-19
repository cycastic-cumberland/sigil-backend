package net.cycastic.sigil.application.pm.task.create;

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
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.model.pm.TaskPriority;

@Data
@RetryOnStale // Latest task ID might stale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class CreateTaskCommand implements Command<IdDto>, PartitionChecksum {
    @Nullable
    private Integer kanbanBoardId;

    @Nullable
    private Long taskStatusId;

    @Nullable
    private TaskPriority taskPriority;

    @NotEmpty
    @Base64String
    private String encryptedName;

    @NotEmpty
    @Base64String
    private String partitionChecksum;

    @Nullable
    @Base64String
    private String encryptedContent;

    @NotEmpty
    @Base64String
    private String iv;
}
