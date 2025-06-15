package net.cycastic.portfoliotoolkit.application.auth.self;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.dto.UserDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSelfCommandHandler implements Command.Handler<GetSelfCommand, UserDto> {
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public UserDto handle(GetSelfCommand command) {
        var user = userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        return UserDto.fromDomain(user);
    }
}
