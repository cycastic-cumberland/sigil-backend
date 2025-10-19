package net.cycastic.sigil.application.feature.get;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BaseGetFeatureCommand {
    @NotEmpty
    private String featureId;

    @Nullable
    private Integer tenantId;
}
