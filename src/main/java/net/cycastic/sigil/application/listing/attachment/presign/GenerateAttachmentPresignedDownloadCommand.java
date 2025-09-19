package net.cycastic.sigil.application.listing.attachment.presign;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import jakarta.annotation.Nullable;

@Data
public class GenerateAttachmentPresignedDownloadCommand implements Command<AttachmentPresignedDto> {
    @NotNull
    private String listingPath;
    @NotNull
    private PresignType presignType;
    @Nullable
    @Base64String
    private String keyMd5;
}
