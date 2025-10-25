package net.cycastic.sigil.application.feature.entitlement.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.EntitlementDto;

@Data
@RequireAdmin
public class GetEntitlementCommand implements Command<EntitlementDto> {
    @NotEmpty
    private String entitlementType;

    @Min(1)
    private int tenantId;
}
