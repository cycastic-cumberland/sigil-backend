package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDto {
    private String featureId;

    private Integer tenantId;

    private Map<String, Object> data;
}
