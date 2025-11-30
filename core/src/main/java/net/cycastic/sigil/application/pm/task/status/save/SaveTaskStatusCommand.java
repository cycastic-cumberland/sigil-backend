package net.cycastic.sigil.application.pm.task.status.save;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.model.pm.TaskUniqueStereotype;

@Data
@TransactionalCommand
@Retry(value = Retry.Event.STALE)
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class SaveTaskStatusCommand implements Command<IdDto> {
    @Nullable
    private Long id;

    @Min(1)
    private int kanbanBoardId;

    @NotBlank
    private String statusName;

    @Nullable
    private TaskUniqueStereotype stereotype;
}
