package net.cycastic.portfoliotoolkit.application.auth.refresh;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.dto.CredentialDto;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements Command.Handler<RefreshTokenCommand, CredentialDto> {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCommandHandler.class);
    private final UserRepository userRepository;
    private final JwtIssuer jwtIssuer;
    private final JwtVerifier jwtVerifier;

    @Override
    public CredentialDto handle(RefreshTokenCommand refreshTokenCommand) {
        try {
            var newToken = jwtIssuer.refreshToken(refreshTokenCommand.authToken());
            var claims = jwtVerifier.extractClaims(newToken);
            var subject = claims.getSubject();
            var userId = Integer.parseInt(subject);
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RequestException(404, "Could not found user"));
            return CredentialDto.builder()
                    .userId(userId)
                    .userEmail(user.getEmail())
                    .authToken(newToken)
                    .build();
        } catch (Exception e){
            logger.error("Error caught while refreshing token", e);
            throw new RequestException(401, "Failed to refresh token");
        }
    }
}
