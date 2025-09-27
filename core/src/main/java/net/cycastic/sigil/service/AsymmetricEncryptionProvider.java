package net.cycastic.sigil.service;

import net.cycastic.sigil.domain.dto.auth.PemDto;

public interface AsymmetricEncryptionProvider extends EncryptionProvider {
    PemDto getPublicKey();
}
