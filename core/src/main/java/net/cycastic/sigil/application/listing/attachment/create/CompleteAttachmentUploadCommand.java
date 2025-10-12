package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;

@Data
@Retry(value = Retry.Event.STALE, maxAttempts = 20)
public class CompleteAttachmentUploadCommand implements Command<Void> {
    private int id;
}
