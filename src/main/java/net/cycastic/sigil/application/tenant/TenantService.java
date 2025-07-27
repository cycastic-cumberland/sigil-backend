package net.cycastic.sigil.application.tenant;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.EmailTemplateEngine;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.UrlAccessor;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;
    private final UriPresigner uriPresigner;
    private final UrlAccessor urlAccessor;
    private final EmailTemplateEngine emailTemplateEngine;

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

    @Transactional
    public void inviteToTenant(int tenantId, String userEmail, int permissions){

    }
}
