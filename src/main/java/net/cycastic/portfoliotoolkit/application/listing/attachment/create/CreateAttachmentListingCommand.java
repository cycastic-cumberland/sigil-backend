package net.cycastic.portfoliotoolkit.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedDto;

@Data
public class CreateAttachmentListingCommand implements Command<AttachmentPresignedDto> {
    private String path;

    private String mimeType;
}
