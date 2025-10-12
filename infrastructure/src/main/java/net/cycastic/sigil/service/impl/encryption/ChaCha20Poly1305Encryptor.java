package net.cycastic.sigil.service.impl.encryption;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.Executor;

public class ChaCha20Poly1305Encryptor extends BaseAEADParallelChunkEncryptor {
    public static final String IDENTIFIER = "ChaCha20-Poly1305";
    public final static long DEFAULT_CHUNKS_SIZE = 16 * 1024; // 16 KiB

    public ChaCha20Poly1305Encryptor(@Nullable Executor executor, long chunkSize){
        super(executor, chunkSize);
    }

    public ChaCha20Poly1305Encryptor(@Nullable Executor executor){
        this(executor, DEFAULT_CHUNKS_SIZE);
    }

    @Override
    @SneakyThrows
    protected byte[] decryptChunk(byte[] cipherText, SecretKey secretKey, IvParameterSpec iv) {
        var cipher = Cipher.getInstance("ChaCha20-Poly1305");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher.doFinal(cipherText);
    }

    @Override
    @SneakyThrows
    protected byte[] encryptChunk(byte[] plainText, SecretKey secretKey, IvParameterSpec iv) {
        var cipher = Cipher.getInstance("ChaCha20-Poly1305");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return cipher.doFinal(plainText);
    }

    @Override
    public boolean canDecrypt(String identifier) {
        return IDENTIFIER.equals(identifier);
    }

    @Override
    @SneakyThrows
    public SecretKey createKey() {
        var keyGen = KeyGenerator.getInstance("ChaCha20");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    @Override
    public SecretKey createKey(byte[] encoded) {
        if (encoded.length != 32){
            throw new IllegalArgumentException("encoded");
        }

        return new SecretKeySpec(encoded, "ChaCha20");
    }
}
