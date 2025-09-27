package net.cycastic.sigil.domain.dto.listing;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttachmentUploadDto {
    @NotBlank
    private String path;

    private String mimeType;

    private long contentLength;
}
