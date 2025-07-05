package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class CompleteAttachmentUploadCommand implements Command<@Null Object> {
    private int id;
}
