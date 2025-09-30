package net.cycastic.sigil.application.tenant.members.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUserInvitationBackgroundJob {
    private int tenantId;
    private int inviterId;
    private String email;
    private int permissions;
}
