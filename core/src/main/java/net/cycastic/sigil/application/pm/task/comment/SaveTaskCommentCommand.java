package net.cycastic.sigil.application.pm.task.comment;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.PartitionPermission;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;

@Data
@Retry(value = {Retry.Event.STALE, Retry.Event.INTEGRITY_VIOLATION})
@TransactionalCommand
@PartitionPermission(ApplicationConstants.PartitionPermissions.WRITE)
public class SaveTaskCommentCommand implements Command<IdDto> {
    private Long id;

    @Size(min = 1)
    private String taskId;

    private CipherDto encryptedContent;
}
