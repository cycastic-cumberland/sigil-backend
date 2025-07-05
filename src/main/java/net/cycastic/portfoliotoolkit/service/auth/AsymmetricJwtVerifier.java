package net.cycastic.portfoliotoolkit.service.auth;

import net.cycastic.portfoliotoolkit.domain.dto.JwkDto;

public interface AsymmetricJwtVerifier extends JwtVerifier {
    JwkDto getJwk();
}
