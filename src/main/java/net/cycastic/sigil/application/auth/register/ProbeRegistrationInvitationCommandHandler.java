package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.auth.InvitationProbeResultDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProbeRegistrationInvitationCommandHandler implements Command.Handler<ProbeRegistrationInvitationCommand, InvitationProbeResultDto> {
    private final UserRepository userRepository;

    @Override
    public InvitationProbeResultDto handle(ProbeRegistrationInvitationCommand command) {
        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> RequestException.withExceptionCode("C404T000"));
        if (user.getStatus() == UserStatus.ACTIVE){
            throw RequestException.withExceptionCode("C400T013");
        }
        return InvitationProbeResultDto.builder()
                .email(user.getEmail())
                .build();
    }
}
