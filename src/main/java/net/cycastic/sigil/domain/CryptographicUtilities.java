package net.cycastic.sigil.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.springframework.lang.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CryptographicUtilities {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final int NONCE_LENGTH = 12; // 96-bit
    public static final int KEY_LENGTH = 32; // 256-bit

    @Data
    @AllArgsConstructor
    public static class EncryptionResult{
        private byte[] cipher;
        private byte[] iv;
    }

    public static byte[] deriveKey(int outputKeyLength, @NotNull byte[] ikm, @Nullable byte[] salt){
        var okm = new byte[outputKeyLength];
        var hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(ikm, salt, null));
        hkdf.generateBytes(okm, 0, okm.length);
        return okm;
    }

    @SneakyThrows
    public static EncryptionResult encrypt(Key key, byte[] data){
        if (!key.getAlgorithm().equals("AES")){
            throw new IllegalStateException("Invalid encryption key");
        }
        var iv = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(iv);
        var gcmSpec = new GCMParameterSpec(128, iv);
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        var encryptedData = cipher.doFinal(data);
        return new EncryptionResult(encryptedData, iv);
    }

    @SneakyThrows
    public static byte[] decrypt(Key key, byte[] iv, byte[] encryptedData){
        if (!key.getAlgorithm().equals("AES")){
            throw new IllegalStateException("Invalid encryption key");
        }
        if (iv.length != NONCE_LENGTH){
            throw new IllegalStateException("Invalid IV");
        }

        var gcmSpec = new GCMParameterSpec(128, iv);
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(encryptedData);
    }

    @SneakyThrows
    public static byte[] digestSha256(byte[] data){
        var md = MessageDigest.getInstance("SHA-256");
        return  md.digest(data);
    }
}
