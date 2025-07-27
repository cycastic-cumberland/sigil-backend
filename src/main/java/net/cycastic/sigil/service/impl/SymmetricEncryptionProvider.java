package net.cycastic.sigil.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.security.SymmetricEncryptionConfiguration;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SymmetricEncryptionProvider implements EncryptionProvider, DecryptionProvider {
    private final SecretKeySpec key;

    public SymmetricEncryptionProvider(SymmetricEncryptionConfiguration configuration){
        var okm = CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH, configuration.getIkm(), configuration.getSalt());
        key = new SecretKeySpec(okm, "AES");
    }

    @SneakyThrows
    private String encryptInternal(byte @NotNull [] unencryptedData){
        var encryptionResult = CryptographicUtilities.encrypt(key, unencryptedData);

        var sb = new StringBuilder("aes256-gcm96$");
        sb.append(Base64.getEncoder().encodeToString(encryptionResult.getIv())).append('$');
        sb.append(Base64.getEncoder().encodeToString(encryptionResult.getCipher()));
        return sb.toString();
    }

    @SneakyThrows
    private byte[] decryptInternal(@NotNull String encryptedData){
        var fragment = encryptedData.split("\\$");
        if (fragment.length != 3){
            throw new UnsupportedOperationException("Unsupported cipher text");
        }
        if (!fragment[0].equals("aes256-gcm96")){
            throw new UnsupportedOperationException("Unsupported cipher type: " + fragment[0]);
        }
        var iv = Base64.getDecoder().decode(fragment[1].getBytes(StandardCharsets.UTF_8));
        var cipherText = Base64.getDecoder().decode(fragment[2].getBytes(StandardCharsets.UTF_8));
        return CryptographicUtilities.decrypt(key, iv, cipherText);
    }

    @Override
    public byte @NotNull [] encrypt(byte @NotNull [] unencryptedData) {
        return encryptInternal(unencryptedData).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt(String plainText) {
        return encryptInternal(plainText.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte @NotNull [] decrypt(byte @NotNull [] encryptedData) {
        return decryptInternal(new String(encryptedData, StandardCharsets.UTF_8));
    }

    @Override
    public String decrypt(String cipherText) {
        return new String(decryptInternal(cipherText));
    }
}
