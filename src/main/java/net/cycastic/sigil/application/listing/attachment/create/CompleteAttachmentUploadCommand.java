package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.RetryOnStale;

@Data
@RetryOnStale
public class CompleteAttachmentUploadCommand implements Command<Void> {
    private int id;
}
