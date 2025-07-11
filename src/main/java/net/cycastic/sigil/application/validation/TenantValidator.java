package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantValidator implements CommandValidator{
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final PartitionUserRepository partitionUserRepository;

    @Override
    public void validate(Command command) {
        var tenantIdOpt = loggedUserAccessor.tryGetTenantId();
        if (tenantIdOpt.isEmpty()){
            return;
        }

        if (!tenantUserRepository.existsByTenant_IdAndUser_Id(tenantIdOpt.getAsInt(), loggedUserAccessor.getUserId())){
            throw RequestException.forbidden();
        }

        var partitionIdOpt = loggedUserAccessor.tryGetPartitionId();
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
    public boolean matches(Command command) {
        return true;
    }
}
