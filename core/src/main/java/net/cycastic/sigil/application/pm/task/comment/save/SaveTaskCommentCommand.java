package net.cycastic.sigil.application.pm.task.comment.save;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;

@Data
@Retry(value = {Retry.Event.STALE, Retry.Event.INTEGRITY_VIOLATION})
@TransactionalCommand
public class SaveTaskCommentCommand implements Command<IdDto> {
    @Nullable
    private Long id;

    @NotBlank
    @Base64String
    private String partitionChecksum;

    @NotEmpty
    private String taskId;

    @NotNull
    private CipherDto encryptedContent;
}
