package net.cycastic.sigil.service;

import net.cycastic.sigil.domain.dto.PemDto;

public interface AsymmetricEncryptionProvider extends EncryptionProvider {
    PemDto getPublicKey();
}
