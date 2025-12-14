package net.cycastic.sigil.application.tenant.members.remove;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJob;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveTenantMemberAssociatedPartitionMembershipBackgroundJob implements BackgroundJob {
    private int userId;
    private int tenantId;
}
