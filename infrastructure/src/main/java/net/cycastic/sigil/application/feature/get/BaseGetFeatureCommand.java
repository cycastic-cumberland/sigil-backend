package net.cycastic.sigil.application.feature.get;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseGetFeatureCommand {
    @NotEmpty
    private String featureId;

    @Nullable
    private Integer tenantId;
}
