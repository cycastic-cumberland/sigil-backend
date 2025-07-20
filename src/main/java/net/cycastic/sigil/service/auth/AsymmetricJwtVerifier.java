package net.cycastic.sigil.service.auth;

import net.cycastic.sigil.domain.dto.auth.JwkDto;

public interface AsymmetricJwtVerifier extends JwtVerifier {
    JwkDto getJwk();
}
