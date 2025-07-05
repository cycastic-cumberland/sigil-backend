package net.cycastic.sigil.domain.model;

public enum CipherEncryptionMethod {
    USER_PASSWORD,
    UNWRAPPED_USER_KEY,
    UNWRAPPED_TENANT_KEY,
    SERVER_SIDE,
}
