package net.cycastic.portfoliotoolkit.application.auth.refresh;

import an.awesome.pipelinr.Command;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.model.UserStatus;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;
import net.cycastic.portfoliotoolkit.service.auth.JwtIssuer;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements Command.Handler<RefreshTokenCommand, CredentialDto> {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCommandHandler.class);
    private final UserRepository userRepository;
    private final JwtIssuer jwtIssuer;
    private final JwtVerifier jwtVerifier;

    private static void verifyCurrentStatus(Claims claims, User user){
        if (!user.isEnabled()){
            throw new ForbiddenException();
        }
        var currentStamp = Base64.getEncoder().encodeToString(user.getSecurityStamp());
        if (!claims.containsKey(ApplicationConstants.SECURITY_STAMP_ENTRY) ||
            !(claims.get(ApplicationConstants.SECURITY_STAMP_ENTRY) instanceof String claimedStamp) ||
            !currentStamp.equals(claimedStamp)){
            throw new ForbiddenException("For your account's safety, please sign in again");
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
