package net.cycastic.sigil.application.tenant.members.remove;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveTenantMemberAssociatedPartitionMembershipBackgroundJobHandler implements BackgroundJobHandler<RemoveTenantMemberAssociatedPartitionMembershipBackgroundJob> {
    private final PartitionUserRepository partitionUserRepository;

    @Override
    @Transactional
    public void process(RemoveTenantMemberAssociatedPartitionMembershipBackgroundJob data) {
        partitionUserRepository.removeByPartition_Tenant_IdAndUser_Id(data.getTenantId(), data.getUserId());
    }
}
