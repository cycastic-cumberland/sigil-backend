package net.cycastic.sigil.application.feature.entitlement.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;

@Data
@RequireAdmin
public class DeleteEntitlementCommand implements Command<Void> {
    @NotEmpty
    private String entitlementType;

    @Min(1)
    private int tenantId;
}
