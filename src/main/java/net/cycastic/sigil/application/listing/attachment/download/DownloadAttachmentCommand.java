package net.cycastic.sigil.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.service.InputStreamResponse;

@Data
public class DownloadAttachmentCommand implements Command<InputStreamResponse> {
    @NotNull
    private String listingPath;
}
