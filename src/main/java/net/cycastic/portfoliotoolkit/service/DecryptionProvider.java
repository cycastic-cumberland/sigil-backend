package net.cycastic.portfoliotoolkit.service;

import jakarta.validation.constraints.NotNull;

public interface DecryptionProvider {
    @NotNull String decrypt(@NotNull String cipherText);
}
