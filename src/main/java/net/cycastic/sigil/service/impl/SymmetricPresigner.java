package net.cycastic.sigil.service.impl;

import jakarta.transaction.NotSupportedException;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.SymmetricPresignerConfiguration;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.service.Presigner;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SymmetricPresigner implements Presigner {
    private static final int KEY_LENGTH = 32; // 256-bit
    private static final String DEFAULT_ALGORITHM = "hmac-sha256";
    private final KeyParameter key;

    public SymmetricPresigner(SymmetricPresignerConfiguration configuration){
        var okm = CryptographicUtilities.deriveKey(KEY_LENGTH, configuration.getIkm(), configuration.getSalt());
        key = new KeyParameter(okm);
    }

    @Override
    @SneakyThrows
    public String getSignature(String data, String algorithm) {
        if (!algorithm.equals(DEFAULT_ALGORITHM)){
            throw new NotSupportedException(String.format("Unsupported presign algorithm: %s", algorithm));
        }

        var message = data.getBytes(StandardCharsets.UTF_8);
        var hmac = new HMac(new SHA256Digest());
        hmac.init(key);
        hmac.update(message, 0, message.length);

        var result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    @SneakyThrows
    public boolean verifySignature(String data, String signature, String algorithm) {
        var calculatedSig = getSignature(data, algorithm);
        return calculatedSig.equals(signature);
    }

    @Override
    public String getDefaultAlgorithm() {
        return DEFAULT_ALGORITHM;
    }
}
