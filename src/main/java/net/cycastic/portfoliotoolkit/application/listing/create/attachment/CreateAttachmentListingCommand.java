package net.cycastic.portfoliotoolkit.application.listing.create.attachment;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;

@Data
public class CreateAttachmentListingCommand implements Command<AttachmentPresignedUploadDto> {
    private String path;

    private String mimeType;
}
