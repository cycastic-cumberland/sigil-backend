package net.cycastic.sigil.application.tenant;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.*;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;

    public void createTenant(User user, String tenantName, UsageType usageType){
        var tenant = Tenant.builder()
                .name(tenantName)
                .usageType(usageType)
                .owner(user)
                .createdAt(OffsetDateTime.now())
                .build();
        tenantRepository.save(tenant);
        inviteToTenant(tenant, user, ApplicationConstants.TenantPermissions.MEMBER);
    }

    public void inviteToTenant(Tenant tenant, User invitee, int permission){
        var tenantUser = TenantUser.builder()
                .tenant(tenant)
                .user(invitee)
                .permissions(permission)
                .build();
        tenantUserRepository.save(tenantUser);
    }

    public void checkPermission(int mask){
        var tenant = getTenant();
        if (tenant.getOwner().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }

        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(loggedUserAccessor.getTenantId(), loggedUserAccessor.getUserId())
                .orElseThrow(ForbiddenException::new);
        if ((tenantUser.getPermissions() & mask) == 0){
            throw new ForbiddenException();
        }
    }

    public Tenant getTenant(){
        return tenantRepository.findById(loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));
    }
}
