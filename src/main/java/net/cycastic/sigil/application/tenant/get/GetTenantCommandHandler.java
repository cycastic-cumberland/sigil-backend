package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.tenant.TenantDto;
import net.cycastic.sigil.domain.dto.tenant.TenantMembership;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTenantCommandHandler implements Command.Handler<GetTenantCommand, TenantDto> {
    private final TenantService tenantService;
    private final UserService userService;

    @Override
    public TenantDto handle(GetTenantCommand command) {
        var tenant = tenantService.getTenant();
        var user = userService.getUser();
        var dto = TenantDto.fromDomain(tenant);
        var permissions = tenantService.getTenantUserPermissions();
        if (user.getId().equals(tenant.getOwner().getId())){
            dto.setMembership(TenantMembership.OWNER);
        } else if ((permissions & ApplicationConstants.TenantPermissions.MODERATE) == ApplicationConstants.TenantPermissions.MODERATE){
            dto.setMembership(TenantMembership.MODERATOR);
        }
        dto.setPermissions(ApplicationConstants.TenantPermissions.toReadablePermissions(permissions));

        return dto;
    }
}
