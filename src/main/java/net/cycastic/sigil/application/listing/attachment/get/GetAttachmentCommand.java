package net.cycastic.sigil.application.listing.attachment.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.listing.AttachmentDto;

@Data
public class GetAttachmentCommand implements Command<AttachmentDto> {
    @NotEmpty
    private String listingPath;
}
