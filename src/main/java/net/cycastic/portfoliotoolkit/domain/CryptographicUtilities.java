package net.cycastic.portfoliotoolkit.domain;

import jakarta.validation.constraints.NotNull;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.springframework.lang.Nullable;

public class CryptographicUtilities {
    public static byte[] deriveKey(int outputKeyLength, @NotNull byte[] ikm, @Nullable byte[] salt){
        var okm = new byte[outputKeyLength];
        var hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(ikm, salt, null));
        hkdf.generateBytes(okm, 0, okm.length);
        return okm;
    }
}
