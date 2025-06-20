package net.cycastic.portfoliotoolkit.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedDto;
import org.springframework.lang.Nullable;

@Data
public class GenerateAttachmentPresignedDownloadCommand implements Command<AttachmentPresignedDto> {
    private @Nullable Integer projectId;
    private @NotNull String listingPath;
}
