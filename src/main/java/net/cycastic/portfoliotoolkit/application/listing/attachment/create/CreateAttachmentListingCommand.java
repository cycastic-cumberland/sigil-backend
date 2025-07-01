package net.cycastic.portfoliotoolkit.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedDto;
import net.cycastic.portfoliotoolkit.domain.dto.listing.AttachmentUploadDto;

public class CreateAttachmentListingCommand extends AttachmentUploadDto implements Command<AttachmentPresignedDto> {
}
