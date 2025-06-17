package net.cycastic.portfoliotoolkit.service.auth;

import lombok.NonNull;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface AsymmetricKeyGenerator {
    record KeyPair(PublicKey publicKey, PrivateKey privateKey){}

    @NonNull KeyPair generate();
}
