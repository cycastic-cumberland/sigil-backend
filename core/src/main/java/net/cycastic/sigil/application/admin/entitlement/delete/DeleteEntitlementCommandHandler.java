package net.cycastic.sigil.application.admin.entitlement.delete;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.feign.feature.EntitlementClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteEntitlementCommandHandler implements Command.Handler<DeleteEntitlementCommand, Void> {
    private final EntitlementClient entitlementClient;

    @Override
    public Void handle(DeleteEntitlementCommand command) {
        entitlementClient.deleteEntitlement(command.getEntitlementType(), command.getTenantId());
        return null;
    }
}
