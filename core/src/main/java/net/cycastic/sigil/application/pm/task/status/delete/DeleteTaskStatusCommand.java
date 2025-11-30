package net.cycastic.sigil.application.pm.task.status.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;

import java.util.Arrays;
import java.util.List;

@Data
@TransactionalCommand
@Retry(value = Retry.Event.STALE)
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class DeleteTaskStatusCommand implements Command<Void> {
    @Min(1)
    private int kanbanBoardId;

    private String statusIds;

    @NotNull
    @Size(min = 1)
    public List<Long> getDeserializedStatusIds(){
        if (statusIds == null){
            return List.of();
        }
        return Arrays.stream(statusIds.split(","))
                .map(String::trim)
                .map(ApplicationUtilities::tryParseLong) // Stream<OptionalLong>
                .flatMap(o -> o.stream().boxed())
                .toList();
    }
}
