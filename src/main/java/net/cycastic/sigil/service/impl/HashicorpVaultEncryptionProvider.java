package net.cycastic.sigil.service.impl;

import com.bettercloud.vault.VaultConfig;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.HashicorpVaultConfiguration;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class HashicorpVaultEncryptionProvider extends HashicorpVaultService implements EncryptionProvider, DecryptionProvider {
    private static final Logger logger = LoggerFactory.getLogger(HashicorpVaultEncryptionProvider.class);

    public HashicorpVaultEncryptionProvider(HashicorpVaultConfiguration configuration){
        this(buildConfig(configuration), configuration.getEncryptionKeyName());
    }

    protected HashicorpVaultEncryptionProvider(VaultConfig vaultConfig, String keyName){
        super(vaultConfig, keyName);
    }

    @SneakyThrows
    protected @NotNull String encryptInternal(byte @NotNull [] unencryptedData){
        var base64 = Base64.getEncoder().encodeToString(unencryptedData);
        Map<String, Object> encryptData = Collections.singletonMap("plaintext", base64);
        var encResp = vault.logical()
                .write(String.format("transit/encrypt/%s", ApplicationUtilities.encodeURIComponent(keyName)), encryptData);
        var cipherText = encResp.getData().get("ciphertext");
        if (cipherText == null){
            logger.error("Failed to encrypt password. Rest response: {}",
                    new String(encResp.getRestResponse().getBody(), StandardCharsets.UTF_8));
            throw new RequestException(500, "Failed to encrypt text");
        }
        return cipherText;
    }

    @SneakyThrows
    protected byte @NotNull [] decryptInternal(@NotNull String encryptedData){
        Map<String, Object> decryptData = Collections.singletonMap("ciphertext", encryptedData);
        var decResp = vault.logical()
                .write(String.format("transit/decrypt/%s", ApplicationUtilities.encodeURIComponent(keyName)), decryptData);
        var b64Decoded = decResp.getData().get("plaintext");
        if (b64Decoded == null){
            logger.error("Failed to decrypt password. Rest response: {}",
                    new String(decResp.getRestResponse().getBody(), StandardCharsets.UTF_8));
            throw new RequestException(500, "Failed to decrypt cipher");
        }
        return Base64.getDecoder().decode(b64Decoded);
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
    @SneakyThrows
    public String decrypt(String cipherText) {
        var decoded = decryptInternal(cipherText);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
