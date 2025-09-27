package net.cycastic.sigil.domain.model;

public enum CipherDecryptionMethod {
    USER_PASSWORD,
    UNWRAPPED_USER_KEY,
    SERVER_SIDE,
    UNWRAPPED_PARTITION_KEY,
    WEBAUTHN_KEY,
}
