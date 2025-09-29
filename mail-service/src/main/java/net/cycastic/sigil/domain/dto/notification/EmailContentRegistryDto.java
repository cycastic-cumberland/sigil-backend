package net.cycastic.sigil.domain.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailContentRegistryDto {
    private String indexRelativePath;
    private Collection<EmailImageEntryDto> imageEntries;
}
