package net.cycastic.sigil.application.listing.attachment.replace;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@Data
@TransactionalCommand
public class OverwriteAttachmentListingCommand implements Command<Void> {
    private String sourcePath;
    private String destinationPath;
}
