package net.cycastic.sigil.domain.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class EmailContentRegistryDto {
    private String indexRelativePath;
    private Collection<EmailImageEntryDto> imageEntries;
}
