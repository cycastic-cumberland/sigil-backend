package net.cycastic.sigil.application.listing.attachment.replace;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;

@Data
@TransactionalCommand
public class OverwriteAttachmentListingCommand implements Command<Void> {
    private String sourcePath;
    private String destinationPath;
}
