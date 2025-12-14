package net.cycastic.sigil.application.tenant.members.remove;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.job.JobScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveTenantMemberCommandHandler implements Command.Handler<RemoveTenantMemberCommand, Void> {
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final JobScheduler jobScheduler;
    private final TenantService tenantService;

    @Override
    public Void handle(RemoveTenantMemberCommand command) {
        var tenantUser = tenantUserRepository.findByTenantIdAndUserEmail(loggedUserAccessor.getTenantId(), command.getEmail())
                .orElseThrow(() -> new RequestException(404, "Tenantship not found"));
        var tenant = tenantService.getTenant();
        if (tenant.getOwner().getId().equals(tenantUser.getUser().getId())){
            // TODO: Implement tenant ownership transfer
            throw new RequestException(403, "Cannot remove tenant owner");
        }
        tenantUserRepository.delete(tenantUser);
        jobScheduler.defer(RemoveTenantMemberAssociatedPartitionMembershipBackgroundJob.builder()
                        .tenantId(loggedUserAccessor.getTenantId())
                        .userId(tenantUser.getUser().getId())
                .build());
        return null;
    }
}
