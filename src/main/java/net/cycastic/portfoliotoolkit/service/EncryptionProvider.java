package net.cycastic.portfoliotoolkit.service;

import jakarta.validation.constraints.NotNull;

public interface EncryptionProvider {
    @NotNull String encrypt(@NotNull String plainText);
}
