package net.cycastic.portfoliotoolkit.service.impl;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.HashicorpVaultConfiguration;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.DecryptionProvider;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Lazy
@Service
public class HashicorpVaultEncryptionProvider implements EncryptionProvider, DecryptionProvider {
    private static final Logger logger = LoggerFactory.getLogger(HashicorpVaultEncryptionProvider.class);
    protected final Vault vault;
    private final String keyName;

    protected HashicorpVaultEncryptionProvider(VaultConfig vaultConfig, String keyName){
        this.vault = new Vault(vaultConfig);
        this.keyName = keyName;
    }

    @Autowired
    public HashicorpVaultEncryptionProvider(HashicorpVaultConfiguration configuration){
        this(buildConfig(configuration), configuration.getKeyName());
    }

    @SneakyThrows
    protected static VaultConfig buildConfig(HashicorpVaultConfiguration configuration){
        return new VaultConfig()
                .address(configuration.getApiAddress())
                .token(configuration.getToken())
                .engineVersion(configuration.getApiVersion())
                .build();
    }

    @Override
    @SneakyThrows
    public String decrypt(String cipherText) {
        Map<String, Object> decryptData = Collections.singletonMap("ciphertext", cipherText);
        var decResp = vault.logical()
                .write(String.format("transit/decrypt/%s", keyName), decryptData);
        var b64Decoded = decResp.getData().get("plaintext");
        if (b64Decoded == null){
            logger.error("Failed to decrypt password. Rest response: {}",
                    new String(decResp.getRestResponse().getBody(), StandardCharsets.UTF_8));
            throw new RequestException(500, "Failed to decrypt cipher");
        }
        byte[] decoded = Base64.getDecoder().decode(b64Decoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    @Override
    @SneakyThrows
    public String encrypt(String plainText) {
        var base64 = Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> encryptData = Collections.singletonMap("plaintext", base64);
        var encResp = vault.logical()
                .write(String.format("transit/encrypt/%s", keyName), encryptData);
        var cipherText = encResp.getData().get("ciphertext");
        if (cipherText == null){
            logger.error("Failed to encrypt password. Rest response: {}",
                    new String(encResp.getRestResponse().getBody(), StandardCharsets.UTF_8));
            throw new RequestException(500, "Failed to encrypt text");
        }
        return cipherText;
    }
}
