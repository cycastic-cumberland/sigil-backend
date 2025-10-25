package net.cycastic.sigil.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntitlementDto {
    @NotNull
    private String entitlementType;

    @Min(1)
    private int tenantId;

    @Nullable
    private JsonNode data;
}
