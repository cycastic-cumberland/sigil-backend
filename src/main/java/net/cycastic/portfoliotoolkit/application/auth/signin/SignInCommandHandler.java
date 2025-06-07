package net.cycastic.portfoliotoolkit.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.dto.CredentialDto;
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
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtIssuer jwtIssuer;

    @Override
    public CredentialDto handle(SignInCommand signInCommand) {
        var user = userRepository.getByEmail(signInCommand.email());
        if (user == null ||
            user.getPassword() == null ||
            !passwordHasher.verify(signInCommand.password(), user.getPassword())){
            throw new RequestException(401, "Incorrect credential");
        }

        var roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        var additionalClaims = new HashMap<String, Object>();
        additionalClaims.put(ApplicationConstants.ROLES_ENTRY, roles);
        additionalClaims.put(ApplicationConstants.SECURITY_STAMP_ENTRY, Base64.getEncoder().encodeToString(user.getSecurityStamp()));
        var token = jwtIssuer.generateTokens(user.getId().toString(), additionalClaims);
        return CredentialDto.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .authToken(token)
                .build();
    }
}
