package net.cycastic.sigil.application.admin.entitlement.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import net.cycastic.sigil.service.feign.feature.EntitlementClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEntitlementCommandHandler implements Command.Handler<GetEntitlementCommand, EntitlementDto> {
    private final EntitlementClient entitlementClient;

    @Override
    public EntitlementDto handle(GetEntitlementCommand command) {
        return entitlementClient.getEntitlement(command.getEntitlementType(), command.getTenantId());
    }
}
