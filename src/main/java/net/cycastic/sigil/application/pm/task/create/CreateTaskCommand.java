package net.cycastic.sigil.application.pm.task.create;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.IdDto;

@Data
@RetryOnStale // Latest task ID might stale
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class CreateTaskCommand implements Command<IdDto> {
    @Nullable
    private Integer kanbanBoardId;

    @Nullable
    private Long taskStatusId;

    @NotEmpty
    private String encryptedName;

    @NotEmpty
    private String encryptedContent;

    @NotEmpty
    private String iv;
}
