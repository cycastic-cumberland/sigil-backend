package net.cycastic.portfoliotoolkit.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.auth.UserService;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;
import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SignInCommandHandler implements Command.Handler<SignInCommand, CredentialDto> {
    private final UserService userService;

    @Override
    public CredentialDto handle(SignInCommand signInCommand) {
        return userService.generateCredential(signInCommand.getEmail(), signInCommand.getPassword());
    }
}
