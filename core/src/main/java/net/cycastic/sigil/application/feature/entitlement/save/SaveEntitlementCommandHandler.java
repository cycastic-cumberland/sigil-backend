package net.cycastic.sigil.application.feature.entitlement.save;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.feign.feature.EntitlementClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveEntitlementCommandHandler implements Command.Handler<SaveEntitlementCommand, Void> {
    private final EntitlementClient entitlementClient;

    @Override
    public Void handle(SaveEntitlementCommand command) {
        entitlementClient.saveEntitlement(command);
        return null;
    }
}
