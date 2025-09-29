package net.cycastic.sigil.domain.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailImageEntryDto {
    private String name;
    private String mappingName;
    private String mimeType;
    private String relativePath;
}
