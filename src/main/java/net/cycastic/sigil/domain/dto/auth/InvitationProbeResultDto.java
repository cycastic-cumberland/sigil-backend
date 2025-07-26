package net.cycastic.sigil.domain.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvitationProbeResultDto {
    private String email;
}
