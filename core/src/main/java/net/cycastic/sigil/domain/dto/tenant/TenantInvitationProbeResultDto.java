package net.cycastic.sigil.domain.dto.tenant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantInvitationProbeResultDto {
    private String email;
    private boolean isActive;
}
