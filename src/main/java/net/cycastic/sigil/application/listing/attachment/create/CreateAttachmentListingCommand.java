package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.dto.listing.AttachmentUploadDto;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateAttachmentListingCommand extends AttachmentUploadDto implements Command<AttachmentPresignedDto> {
    @Nullable
    private String keyMd5;
}
