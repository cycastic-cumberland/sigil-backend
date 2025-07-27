package net.cycastic.sigil.application.tenant.members.invite;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationParams;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationProbeResultDto;

public class ProbeTenantInvitationCommand extends TenantInvitationParams implements Command<TenantInvitationProbeResultDto> {
}
