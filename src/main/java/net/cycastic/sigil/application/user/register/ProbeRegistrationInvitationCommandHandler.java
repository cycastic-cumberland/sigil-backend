package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.auth.UserInvitationProbeResultDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProbeRegistrationInvitationCommandHandler implements Command.Handler<ProbeRegistrationInvitationCommand, UserInvitationProbeResultDto> {
    private final UserRepository userRepository;

    @Override
    public UserInvitationProbeResultDto handle(ProbeRegistrationInvitationCommand command) {
        var user = userRepository.findById(command.getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> RequestException.withExceptionCode( "C404T000"));
        if (user.getStatus() == UserStatus.ACTIVE){
            throw RequestException.withExceptionCode("C400T013");
        }
        return UserInvitationProbeResultDto.builder()
                .email(user.getEmail())
                .build();
    }
}
