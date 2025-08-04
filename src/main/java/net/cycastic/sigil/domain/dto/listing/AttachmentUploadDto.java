package net.cycastic.sigil.domain.dto.listing;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttachmentUploadDto {
    @NotEmpty
    private String path;

    private String mimeType;

    private long contentLength;
}
