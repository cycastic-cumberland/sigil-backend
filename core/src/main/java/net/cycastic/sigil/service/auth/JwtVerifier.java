package net.cycastic.sigil.service.auth;

import io.jsonwebtoken.Claims;
import lombok.NonNull;

public interface JwtVerifier {
    @NonNull Claims extractClaims(@NonNull String jwt);
}
