package net.cycastic.sigil.domain.dto.tenant;

import lombok.Data;

@Data
public class TenantInvitationParams {
    private int userId;
    private int tenantId;
    private int tenantUserId;
    private long notValidBefore;
    private long notValidAfter;
}
