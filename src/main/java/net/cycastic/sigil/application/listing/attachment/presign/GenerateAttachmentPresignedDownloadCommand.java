package net.cycastic.sigil.application.listing.attachment.presign;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import org.springframework.lang.Nullable;

@Data
public class GenerateAttachmentPresignedDownloadCommand implements Command<AttachmentPresignedDto> {
    @NotNull
    private String listingPath;
    @NotNull
    private PresignType presignType;
    @Nullable
    private String keyMd5;
}
