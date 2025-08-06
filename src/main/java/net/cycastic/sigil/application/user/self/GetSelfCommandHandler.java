package net.cycastic.sigil.application.user.self;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSelfCommandHandler implements Command.Handler<GetSelfCommand, UserDto> {
    private final UserService userService;

    @Override
    public UserDto handle(GetSelfCommand command) {
        var user = userService.getUser();
        return userService.getUserDetails(user);
    }
}
