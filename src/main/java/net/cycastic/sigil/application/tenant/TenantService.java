package net.cycastic.sigil.application.tenant;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.TenantUser;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.domain.model.tenant.User;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
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

    public int getTenantUserPermissions() {
        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(loggedUserAccessor.getTenantId(), loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        return tenantUser.getPermissions();
    }

    public void checkPermission(int mask){
        var tenant = getTenant();
        if (tenant.getOwner().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }

        if ((getTenantUserPermissions() & mask) != mask){
            throw RequestException.forbidden();
        }
    }

    public Tenant getTenant(){
        return tenantRepository.findById(loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));
    }
}
