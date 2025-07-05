package net.cycastic.sigil.application.email.template;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMailTemplateFromUploadCommand implements Command<AttachmentPresignedDto> {
    @NotNull
    private String templateName;

    private String listingPath;

    private String fileName;

    private String mimeType;
}
