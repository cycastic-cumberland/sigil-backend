package net.cycastic.portfoliotoolkit.service;

import jakarta.validation.constraints.NotNull;

public interface DecryptionProvider {
    byte @NotNull [] decrypt(byte @NotNull [] encryptedData);
    @NotNull String decrypt(@NotNull String cipherText);
}
