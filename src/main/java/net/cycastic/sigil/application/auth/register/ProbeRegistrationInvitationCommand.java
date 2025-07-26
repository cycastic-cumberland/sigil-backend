package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.application.presigned.PresignedRequest;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationParams;
import net.cycastic.sigil.domain.dto.auth.InvitationProbeResultDto;

public class ProbeRegistrationInvitationCommand extends CompleteUserRegistrationParams implements PresignedRequest, Command<InvitationProbeResultDto> {
}
