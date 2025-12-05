package net.cycastic.sigil.application.admin.entitlement.save;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.EntitlementDto;

@RequireAdmin
public class SaveEntitlementCommand extends EntitlementDto implements Command<Void> {
}
