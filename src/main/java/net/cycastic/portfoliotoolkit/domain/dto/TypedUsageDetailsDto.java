package net.cycastic.portfoliotoolkit.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.UsageType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TypedUsageDetailsDto extends UsageDetailsDto {
    private UsageType usageType;
}
