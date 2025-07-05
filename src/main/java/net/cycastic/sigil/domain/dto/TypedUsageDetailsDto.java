package net.cycastic.sigil.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.UsageType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TypedUsageDetailsDto extends UsageDetailsDto {
    private UsageType usageType;
}
