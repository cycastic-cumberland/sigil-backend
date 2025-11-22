package net.cycastic.sigil.application.pm.task.transit;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TransactionalCommand
@Retry(value = Retry.Event.STALE)
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class MoveTasksCommand implements Command<Void> {
    @NotNull
    private Map<String, Long> tasks;

    @NotNull
    private Integer kanbanBoardId;
}
