package net.cycastic.portfoliotoolkit.service.auth;

import lombok.NonNull;

import java.util.Map;

public interface JwtVerifier {
    @NonNull Map<String, Object> extractClaims(@NonNull String jwt);
}
