package net.cycastic.portfoliotoolkit.service.auth;

import lombok.NonNull;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface AsymmetricKeyGenerator {
    public static record KeyPair(PublicKey publicKey, PrivateKey privateKey){}

    @NonNull KeyPair generate();
}
