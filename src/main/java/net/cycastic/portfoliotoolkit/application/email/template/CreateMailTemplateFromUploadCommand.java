package net.cycastic.portfoliotoolkit.application.email.template;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMailTemplateFromUploadCommand implements Command<AttachmentPresignedUploadDto> {
    @NotNull
    private String templateName;

    private String listingPath;

    private String fileName;

    private String mimeType;
}
