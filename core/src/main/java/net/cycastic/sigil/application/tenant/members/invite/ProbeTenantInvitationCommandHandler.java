package net.cycastic.sigil.application.tenant.members.invite;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationProbeResultDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProbeTenantInvitationCommandHandler implements Command.Handler<ProbeTenantInvitationCommand, TenantInvitationProbeResultDto> {
    private final UserRepository userRepository;

    @Override
    public TenantInvitationProbeResultDto handle(ProbeTenantInvitationCommand command) {
        var user = userRepository.findById(command.getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> RequestException.withExceptionCode( "C404T000"));
        return TenantInvitationProbeResultDto.builder()
                .email(user.getEmail())
                .isActive(UserStatus.ACTIVE.equals(user.getStatus()))
                .build();
    }
}
