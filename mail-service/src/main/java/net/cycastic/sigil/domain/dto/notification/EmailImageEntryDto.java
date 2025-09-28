package net.cycastic.sigil.domain.dto.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailImageEntryDto {
    private String name;
    private String mimeType;
    private String relativePath;
}
