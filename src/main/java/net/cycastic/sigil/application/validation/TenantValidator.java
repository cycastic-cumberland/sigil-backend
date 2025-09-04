package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.validation.RequireAdmin;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class TenantValidator implements CommandValidator{
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PartitionUserRepository partitionUserRepository;

    @Override
    public void validate(Command command) {
        var tenantIdOpt = loggedUserAccessor.tryGetTenantId();
        var partitionIdOpt = loggedUserAccessor.tryGetPartitionId();
        if (tenantIdOpt.isEmpty()){
            if (partitionIdOpt.isPresent()){
                throw RequestException.forbidden();
            }
            return;
        }

        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(loggedUserAccessor.getTenantId(), loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        if (tenantUser.getLastInvited() != null){
            throw RequestException.forbidden();
        }

        if (partitionIdOpt.isEmpty()){
            return;
        }
        if (!partitionUserRepository.existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(tenantIdOpt.getAsInt(),
                partitionIdOpt.getAsInt(),
                loggedUserAccessor.getUserId())){
            throw RequestException.forbidden();
        }
    }

    @Override
    public boolean matches(Class klass) {
        return true;
    }
}
