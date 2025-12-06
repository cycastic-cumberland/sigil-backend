package net.cycastic.sigil.application.admin.user.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserByIdCommandHandler implements Command.Handler<GetUserByIdCommand, UserDto> {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public UserDto handle(GetUserByIdCommand command) {
        var user = userRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        return userService.getUserDetails(user);
    }
}
