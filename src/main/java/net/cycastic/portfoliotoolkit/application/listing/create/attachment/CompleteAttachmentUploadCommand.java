package net.cycastic.portfoliotoolkit.application.listing.create.attachment;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class CompleteAttachmentUploadCommand implements Command<@Null Object> {
    private int id;
}
