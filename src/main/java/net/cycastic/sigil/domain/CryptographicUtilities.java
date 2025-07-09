package net.cycastic.sigil.domain;

import jakarta.transaction.NotSupportedException;
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
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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
        if (key instanceof RSAPublicKey publicKey){
            var cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return new EncryptionResult(cipher.doFinal(data), null);
        }
        if (!key.getAlgorithm().equals("AES")){
            throw new IllegalStateException("Invalid encryption key");
        }
        var iv = new byte[NONCE_LENGTH];
        generateRandom(iv);
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
    public static byte[] decrypt(Key key, byte[] cipher){
        if (key instanceof RSAPrivateKey privateKey){
            var dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            dec.init(Cipher.DECRYPT_MODE, privateKey);
            return dec.doFinal(cipher);
        }

        throw new NotSupportedException(key.getAlgorithm());
    }

    @SneakyThrows
    public static byte[] digestSha256(byte[] data){
        var md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    @SneakyThrows
    public static byte[] digestMd5(byte[] data){
        var md = MessageDigest.getInstance("MD5");
        return md.digest(data);
    }

    public static void generateRandom(byte[] data){
        RANDOM.nextBytes(data);
    }

    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        var result = 0;
        for (var i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
