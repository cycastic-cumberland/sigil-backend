package net.cycastic.sigil.service.auth;

import jakarta.validation.constraints.Null;
import lombok.NonNull;

import java.util.Map;

public interface JwtIssuer {
    @NonNull String generateTokens(@NonNull String subject, @Null Map<String, Object> extraClaims);
    @NonNull String refreshToken(@NonNull String authToken);
}
