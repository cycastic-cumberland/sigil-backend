package net.cycastic.sigil.service;

import jakarta.validation.constraints.NotNull;

public interface EncryptionProvider {
    byte @NotNull [] encrypt(byte @NotNull [] unencryptedData);
    @NotNull String encrypt(@NotNull String plainText);
}
