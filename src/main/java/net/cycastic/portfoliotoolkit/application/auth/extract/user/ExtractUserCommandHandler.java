package net.cycastic.portfoliotoolkit.application.auth.extract.user;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExtractUserCommandHandler implements Command.Handler<ExtractUserCommand, User> {
    private final JwtVerifier jwtVerifier;
    private final UserRepository userRepository;

    @Override
    public User handle(ExtractUserCommand extractUserCommand) {
        var claims =  jwtVerifier.extractClaims(extractUserCommand.authToken());
        var subject = claims.getSubject();
        var userId = ApplicationUtilities
                .tryParseInt(subject)
                .orElseThrow(() -> new RequestException(404, "User not found"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new RequestException(404, "User not found"));
    }
}
