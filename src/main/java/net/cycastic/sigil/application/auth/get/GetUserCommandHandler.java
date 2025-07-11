package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserCommandHandler implements Command.Handler<GetUserCommand, UserDto> {
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public UserDto handle(GetUserCommand command) {
        var user = userRepository.findByEmailAndTenantId(command.getUserEmail(), loggedUserAccessor.getTenantId())
                .orElseThrow(RequestException::forbidden);
        return UserDto.fromDomain(user);
    }
}
