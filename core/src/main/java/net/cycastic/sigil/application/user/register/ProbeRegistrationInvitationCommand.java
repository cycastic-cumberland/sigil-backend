package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.application.presigned.PresignedRequest;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationParams;
import net.cycastic.sigil.domain.dto.auth.UserInvitationProbeResultDto;

public class ProbeRegistrationInvitationCommand extends CompleteUserRegistrationParams implements PresignedRequest, Command<UserInvitationProbeResultDto> {
}
