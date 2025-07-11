package net.cycastic.sigil.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.dto.CredentialDto;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class SignInCommandHandler implements Command.Handler<SignInCommand, CredentialDto> {
    private final UserService userService;

    @Override
    public CredentialDto handle(SignInCommand command) {
        return userService.generateCredential(command.getEmail(), Base64.getDecoder().decode(command.getHashedPassword()));
    }
}
