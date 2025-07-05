package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.listing.AttachmentUploadDto;

public class CreateAttachmentListingCommand extends AttachmentUploadDto implements Command<AttachmentPresignedDto> {
}
