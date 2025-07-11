package net.cycastic.sigil.application.listing.attachment.upload;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.listing.AttachmentUploadDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class UploadAttachmentCommand extends AttachmentUploadDto implements Command<@Null Object> {
    private FileUpload fileUpload;
}
