package net.cycastic.sigil.application.auth.webauthn.enroll;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollWebAuthnEnvelopCommandHandler implements Command.Handler<EnrollWebAuthnEnvelopCommand, Void> {
    private final UserService userService;

    @Override
    @Transactional
    public Void handle(EnrollWebAuthnEnvelopCommand command) {
        var user = userService.getUser();
        userService.enrollWebAuthnNoTransaction(user, command);
        return null;
    }
}
