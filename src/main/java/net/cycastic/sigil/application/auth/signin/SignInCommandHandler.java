package net.cycastic.sigil.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SignInCommandHandler implements Command.Handler<SignInCommand, CredentialDto> {
    private final UserService userService;

    @Override
    public CredentialDto handle(SignInCommand command) {
        return userService.generateCredential(command.getPayload(),
                Objects.requireNonNullElse(command.getAlgorithm(), "SHA256withRSA/PSS"),
                Base64.getDecoder().decode(command.getSignature()));
    }
}
