package net.cycastic.portfoliotoolkit.service.auth;

import io.jsonwebtoken.Claims;
import lombok.NonNull;

public interface JwtVerifier {
    @NonNull Claims extractClaims(@NonNull String jwt);
}
