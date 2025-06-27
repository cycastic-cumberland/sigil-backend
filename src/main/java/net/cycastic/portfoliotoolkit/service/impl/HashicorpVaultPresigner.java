package net.cycastic.portfoliotoolkit.service.impl;

import jakarta.transaction.NotSupportedException;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.HashicorpVaultConfiguration;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.service.Presigner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@Component
public class HashicorpVaultPresigner extends HashicorpVaultService implements Presigner {
    private static final String VAULT_ALGORITHM_PREFIX = "vault-";
    private static final String DEFAULT_ALGORITHM = "sha2-384";

    public HashicorpVaultPresigner(HashicorpVaultConfiguration configuration){
        super(buildConfig(configuration), configuration.getPresignKeyName());
    }

    @Override
    @SneakyThrows
    public String getSignature(String data, String algorithm) {
        if (!algorithm.startsWith(VAULT_ALGORITHM_PREFIX)){
            throw new NotSupportedException(String.format("Unsupported presign algorithm: %s", algorithm));
        }

        algorithm = algorithm.substring(VAULT_ALGORITHM_PREFIX.length());

        var base64Data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        HashMap<String, Object> signData = HashMap.newHashMap(2);
        signData.put("algorithm", algorithm);
        signData.put("input", base64Data);
        var encResp = vault.logical()
                .write(String.format("transit/hmac/%s", ApplicationUtilities.encodeURIComponent(keyName)), signData);
        return encResp.getData().get("hmac");
    }

    @Override
    @SneakyThrows
    public boolean verifySignature(String data, String signature, String algorithm) {
        if (!algorithm.startsWith(VAULT_ALGORITHM_PREFIX)){
            throw new NotSupportedException(String.format("Unsupported presign algorithm: %s", algorithm));
        }

        algorithm = algorithm.substring(VAULT_ALGORITHM_PREFIX.length());

        var base64Data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        HashMap<String, Object> verifyData = HashMap.newHashMap(3);
        verifyData.put("algorithm", algorithm);
        verifyData.put("input", base64Data);
        verifyData.put("hmac", signature);
        var encResp = vault.logical()
                .write(String.format("transit/verify/%s", ApplicationUtilities.encodeURIComponent(keyName)), verifyData);
        return Boolean.parseBoolean(encResp.getData().get("valid"));
    }

    @Override
    public String getDefaultAlgorithm() {
        return VAULT_ALGORITHM_PREFIX + DEFAULT_ALGORITHM;
    }
}
