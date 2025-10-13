package net.cycastic.sigil.application.tenant.members.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJob;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUserInvitationBackgroundJob implements BackgroundJob {
    private int tenantId;
    private int inviterId;
    private String email;
    private int permissions;
}
