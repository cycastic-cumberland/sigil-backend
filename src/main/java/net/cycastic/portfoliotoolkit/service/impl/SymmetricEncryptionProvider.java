package net.cycastic.portfoliotoolkit.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.SymmetricEncryptionConfiguration;
import net.cycastic.portfoliotoolkit.service.DecryptionProvider;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.springframework.lang.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class SymmetricEncryptionProvider implements EncryptionProvider, DecryptionProvider {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_LENGTH = 12; // 96-bit
    private static final int KEY_LENGTH = 32; // 256-bit
    private final byte[] symmetricKey;

    public SymmetricEncryptionProvider(SymmetricEncryptionConfiguration configuration){
        symmetricKey = extractOkm(configuration.getIkm(), configuration.getSalt());
    }

    protected byte[] extractOkm(@NotNull byte[] ikm, @Nullable byte[] salt){
        var okm = new byte[KEY_LENGTH];
        var hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(ikm, salt, null));
        hkdf.generateBytes(okm, 0, okm.length);
        return okm;
    }

    @SneakyThrows
    private String encryptInternal(byte @NotNull [] unencryptedData){
        var iv = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(iv);
        var key =  new SecretKeySpec(symmetricKey, "AES");
        var gcmSpec = new GCMParameterSpec(128, iv);
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        var encryptedData = cipher.doFinal(unencryptedData);

        var sb = new StringBuilder("aes256-gcm96$");
        sb.append(Base64.getEncoder().encodeToString(iv)).append('$');
        sb.append(Base64.getEncoder().encodeToString(encryptedData));
        return sb.toString();
    }

    @SneakyThrows
    private byte[] decryptInternal(@NotNull String encryptedData){
        var fragment = encryptedData.split("\\$");
        if (fragment.length != 3){
            throw new UnsupportedOperationException("Unsupported cipher text");
        }
        if (!fragment[0].equals("aes256-gcm96")){
            throw new UnsupportedOperationException("Unsupported cipher type: " + fragment[0]);
        }
        var iv = Base64.getDecoder().decode(fragment[1].getBytes(StandardCharsets.UTF_8));
        var cipherText = Base64.getDecoder().decode(fragment[2].getBytes(StandardCharsets.UTF_8));
        var key =  new SecretKeySpec(symmetricKey, "AES");
        var gcmSpec = new GCMParameterSpec(128, iv);
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(cipherText);
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
    public String decrypt(String cipherText) {
        return new String(decryptInternal(cipherText));
    }
}
