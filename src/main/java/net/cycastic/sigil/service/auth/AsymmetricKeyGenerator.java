package net.cycastic.sigil.service.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface AsymmetricKeyGenerator {
    @Data
    @AllArgsConstructor
    class KeyPair {
        private PublicKey publicKey;
        private PrivateKey privateKey;
    }

    String algorithm();

    @NonNull KeyPair generate();
}
