package net.cycastic.sigil.service.impl.encryption;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.Executor;

public class AESGCMEncryptor extends BaseAEADParallelChunkEncryptor{
    public static final String IDENTIFIER = "AES-GCM";
    public static final long DEFAULT_CHUNKS_SIZE = 16 * 1024; // 16 KiB

    public AESGCMEncryptor(@Nullable Executor executor, long chunkSize) {
        super(executor, chunkSize);
    }

    public AESGCMEncryptor(@Nullable Executor executor){
        this(executor, DEFAULT_CHUNKS_SIZE);
    }

    @Override
    @SneakyThrows
    protected byte[] decryptChunk(byte[] cipherText, SecretKey secretKey, IvParameterSpec iv) {
        var gcmSpec = new GCMParameterSpec(128, iv.getIV());
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(cipherText);
    }

    @Override
    @SneakyThrows
    protected byte[] encryptChunk(byte[] plainText, SecretKey secretKey, IvParameterSpec iv) {
        var gcmSpec = new GCMParameterSpec(128, iv.getIV());
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(plainText);
    }

    @Override
    public boolean canDecrypt(String identifier) {
        return IDENTIFIER.equals(identifier);
    }

    @Override
    @SneakyThrows
    public SecretKey createKey() {
        var keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    @Override
    public SecretKey createKey(byte[] encoded) {
        if (encoded.length != 32){
            throw new IllegalArgumentException("encoded");
        }

        return new SecretKeySpec(encoded, "AES");
    }
}
