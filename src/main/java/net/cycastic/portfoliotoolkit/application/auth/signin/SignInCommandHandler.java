package net.cycastic.portfoliotoolkit.application.auth.signin;

import an.awesome.pipelinr.Command;
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
public class SignInCommandHandler implements Command.Handler<SignInCommand, CredentialDto> {
    private static final String DUMMY_TEXT = "Hello World!";
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtIssuer jwtIssuer;
    private final String dummyHash;

    public SignInCommandHandler(UserRepository userRepository, PasswordHasher passwordHasher, JwtIssuer jwtIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtIssuer = jwtIssuer;

        dummyHash = passwordHasher.hash(DUMMY_TEXT);
    }

    private CredentialDto wasteComputePower(){
        passwordHasher.verify(DUMMY_TEXT, dummyHash);
        jwtIssuer.generateTokens("", null);
        throw new RequestException(401, "Incorrect credential");
    }

    @Override
    public CredentialDto handle(SignInCommand signInCommand) {
        var user = userRepository.getByEmail(signInCommand.email());

        if (user == null || user.isDisabled() || user.getPassword() == null){
            return wasteComputePower();
        }

        if (!passwordHasher.verify(signInCommand.password(), user.getPassword())){
            jwtIssuer.generateTokens("", null);
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
