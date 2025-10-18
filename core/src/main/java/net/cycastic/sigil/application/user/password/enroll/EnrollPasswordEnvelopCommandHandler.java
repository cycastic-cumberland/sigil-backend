package net.cycastic.sigil.application.user.password.enroll;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollPasswordEnvelopCommandHandler implements Command.Handler<EnrollPasswordEnvelopCommand, Void> {
    private final UserService userService;

    @Override
    public Void handle(EnrollPasswordEnvelopCommand command) {
        var user = userService.getUser();
        userService.enrollPasswordNoTransaction(user, command);
        userService.invalidateUserKdfCache(user, AuthenticationMethod.PASSWORD);
        userService.invalidateUserEnvelop(user);
        return null;
    }
}
