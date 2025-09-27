package net.cycastic.sigil.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.security.HashicorpVaultConfiguration;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.auth.PemDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.AsymmetricDecryptionProvider;
import net.cycastic.sigil.service.AsymmetricEncryptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;


public class HashicorpVaultEphemeralAsymmetricEncryptionProvider extends HashicorpVaultEncryptionProvider implements AsymmetricEncryptionProvider, AsymmetricDecryptionProvider {
    private static final Logger logger = LoggerFactory.getLogger(HashicorpVaultEphemeralAsymmetricEncryptionProvider.class);
    private final int latestVersion;
    private final String publicKeyPem;
    private final PublicKey publicKey;

    @SneakyThrows
    public HashicorpVaultEphemeralAsymmetricEncryptionProvider(HashicorpVaultConfiguration configuration){
        super(buildConfig(configuration), configuration.getEphemeralKeyName());

        var resp = vault.logical().read(String.format("transit/keys/%s", ApplicationUtilities.encodeURIComponent(keyName)));
        var allKeys = resp.getDataObject().get("keys").asObject();
        latestVersion = allKeys.names().stream()
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(1);
        publicKeyPem = allKeys.get(Integer.toString(latestVersion)).asObject().getString("public_key");
        var cleanPem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        publicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(Base64.getDecoder().decode(cleanPem));
    }

    @Override
    @SneakyThrows
    protected String encryptInternal(byte @NotNull [] unencryptedData) {
        var cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        var cipherText = cipher.doFinal(unencryptedData);
        return String.format("vault:v%d:%s", latestVersion, Base64.getEncoder().encodeToString(cipherText));
    }

    @Override
    @SneakyThrows
    protected byte @NotNull [] decryptInternal(String encryptedData) {
        Map<String, Object> decryptData = Map.of("ciphertext", encryptedData,
                "padding_scheme", "oaep");
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

    public PemDto getPublicKey(){
        return new PemDto(publicKeyPem, latestVersion);
    }
}
