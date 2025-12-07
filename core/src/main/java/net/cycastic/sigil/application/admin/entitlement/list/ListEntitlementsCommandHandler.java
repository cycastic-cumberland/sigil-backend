package net.cycastic.sigil.application.admin.entitlement.list;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import net.cycastic.sigil.domain.dto.paging.EnumerablePage;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.feign.feature.EntitlementClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ListEntitlementsCommandHandler implements Command.Handler<ListEntitlementsCommand, EnumerablePage<EntitlementDto>> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final EntitlementClient entitlementClient;

    @Override
    public EnumerablePage<EntitlementDto> handle(ListEntitlementsCommand command) {
        var tenantId = loggedUserAccessor.getTenantId();
        int pageSize = Objects.requireNonNullElse(command.getPageSize(), 10);
        if (command.getPaginationToken() != null){
            return entitlementClient.listEntitlements(tenantId, pageSize, command.getPaginationToken());
        } else {
            return entitlementClient.listEntitlements(tenantId, pageSize);
        }
    }
}
