package net.cycastic.portfoliotoolkit.domain.dto.listing;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttachmentUploadDto {
    private String path;
    private String mimeType;
    private long contentLength;
}
