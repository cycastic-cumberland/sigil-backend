package net.cycastic.sigil.application.auth.refresh;

import an.awesome.pipelinr.Command;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.User;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.domain.dto.CredentialDto;
import net.cycastic.sigil.service.auth.JwtIssuer;
import net.cycastic.sigil.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements Command.Handler<RefreshTokenCommand, CredentialDto> {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCommandHandler.class);
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtIssuer jwtIssuer;
    private final JwtVerifier jwtVerifier;

    private static void verifyCurrentStatus(Claims claims, User user){
        if (!user.isEnabled()){
            throw RequestException.forbidden();
        }
        var currentStamp = Base64.getEncoder().encodeToString(user.getSecurityStamp());
        if (!claims.containsKey(ApplicationConstants.SECURITY_STAMP_ENTRY) ||
            !(claims.get(ApplicationConstants.SECURITY_STAMP_ENTRY) instanceof String claimedStamp) ||
            !currentStamp.equals(claimedStamp)){
            throw RequestException.withExceptionCode("C403T001");
        }
    }

    @Override
    public CredentialDto handle(RefreshTokenCommand refreshTokenCommand) {
        try {
            var newToken = jwtIssuer.refreshToken(refreshTokenCommand.authToken());
            var claims = jwtVerifier.extractClaims(newToken);
            var subject = claims.getSubject();
            var userId = Integer.parseInt(subject);
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RequestException(404, "Could not find user"));
            verifyCurrentStatus(claims, user);
            return userService.createCredential(user, newToken);
        } catch (Exception e){
            logger.error("Error caught while refreshing token", e);
            throw new RequestException(401, "Failed to refresh token");
        }
    }
}
