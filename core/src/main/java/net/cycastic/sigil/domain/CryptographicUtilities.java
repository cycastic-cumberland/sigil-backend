package net.cycastic.sigil.domain;

import an.awesome.pipelinr.repack.com.google.common.reflect.TypeToken;
import jakarta.transaction.NotSupportedException;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import jakarta.annotation.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

public class CryptographicUtilities {
    private interface Signer<TPublic extends PublicKey, TPrivate extends PrivateKey> {
        byte[] sign(TPrivate key, byte[] data);
        boolean verify(TPublic key, byte[] data, byte[] signature);

        default boolean matchesPublic(PublicKey key) {
            TypeToken<Signer<TPublic, TPrivate>> signerToken = new TypeToken<>(getClass()) {};
            TypeToken<?> publicToken = signerToken.resolveType(Signer.class.getTypeParameters()[0]);
            return publicToken.isSupertypeOf(key.getClass());
        }

        default boolean matchesPrivate(PrivateKey key) {
            TypeToken<Signer<TPublic, TPrivate>> signerToken = new TypeToken<>(getClass()) {};
            TypeToken<?> privateToken = signerToken.resolveType(Signer.class.getTypeParameters()[1]);
            return privateToken.isSupertypeOf(key.getClass());
        }
    }

    private static class RSASSAPSSSigner implements Signer<RSAPublicKey, RSAPrivateKey>{
        public static final RSASSAPSSSigner INSTANCE = new RSASSAPSSSigner();
        private static final PSSParameterSpec STANDARD_PSS_SPEC = new PSSParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                32,
                PSSParameterSpec.TRAILER_FIELD_BC
        );

        @Override
        @SneakyThrows
        public byte[] sign(RSAPrivateKey key, byte[] data) {
            var signer = Signature.getInstance("SHA256withRSA/PSS", "BC");
            signer.setParameter(STANDARD_PSS_SPEC);

            signer.initSign(key);
            signer.update(data);
            return signer.sign();

        }

        @Override
        @SneakyThrows
        public boolean verify(RSAPublicKey key, byte[] data, byte[] signature) {
            var verifier = Signature.getInstance("RSASSA-PSS", "BC");
            verifier.setParameter(STANDARD_PSS_SPEC);
            verifier.initVerify(key);
            verifier.update(data);
            return verifier.verify(signature);
        }
    }

    public static class TOTP {
        public static long getTimeStamp(long timestamp, long window){
            return timestamp / window;
        }
    }

    public static class Keys {
        @SneakyThrows
        public static ECPrivateKey decodeECPrivateKey(byte[] encoded){
            var keySpec = new PKCS8EncodedKeySpec(encoded);
            var kf = KeyFactory.getInstance("EC", "BC");
            return (ECPrivateKey)kf.generatePrivate(keySpec);
        }

        @SneakyThrows
        public static ECPublicKey decodeECPublicKey(byte[] encoded){
            var keySpec = new X509EncodedKeySpec(encoded);
            var kf = KeyFactory.getInstance("EC", "BC");
            return (ECPublicKey)kf.generatePublic(keySpec);
        }

        @SneakyThrows
        public static RSAPrivateKey decodeRSAPrivateKey(byte[] encoded){
            var keySpec = new PKCS8EncodedKeySpec(encoded);
            var kf = KeyFactory.getInstance("RSA", "BC");
            return (RSAPrivateKey)kf.generatePrivate(keySpec);
        }

        @SneakyThrows
        public static RSAPublicKey decodeRSAPublicKey(byte[] encoded){
            var keySpec = new X509EncodedKeySpec(encoded);
            var kf = KeyFactory.getInstance("RSA", "BC");
            return (RSAPublicKey)kf.generatePublic(keySpec);
        }
    }

    private static final Map<String, Signer> SUPPORTED_SIGNING_ALGORITHMS = Map.of("SHA256withRSA/PSS", RSASSAPSSSigner.INSTANCE);
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
    public static byte[] encrypt(Key key, byte[] iv, byte[] data){
        if (!key.getAlgorithm().equals("AES")){
            throw new IllegalStateException("Invalid encryption key");
        }
        if (iv.length != NONCE_LENGTH){
            throw new IllegalStateException("Invalid nonce");
        }
        var gcmSpec = new GCMParameterSpec(128, iv);
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(data);
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
        return new EncryptionResult(encrypt(key, iv, data), iv);
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

    public static byte[] digestMd5(byte[] data){
        return SlimCryptographicUtilities.digestMd5(data);
    }

    @Deprecated
    public static void generateRandom(byte[] data){
        SlimCryptographicUtilities.generateRandom(data);
    }

    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        var result = 0;
        for (var i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    @SneakyThrows
    public static byte[] sign(PrivateKey key, byte[] data, String algorithm){
        var supportedAlgorithms = SUPPORTED_SIGNING_ALGORITHMS.get(algorithm);
        if (supportedAlgorithms == null){
            throw new NotSupportedException(algorithm);
        }
        if (!supportedAlgorithms.matchesPrivate(key)){
            throw new NotSupportedException("Can not sign data with key of type " + key.getClass().getName());
        }
        return supportedAlgorithms.sign(key, data);
    }

    @SneakyThrows
    public static boolean verifySignature(byte[] data, byte[] signature, String algorithm, PublicKey key){
        var supportedAlgorithms = SUPPORTED_SIGNING_ALGORITHMS.get(algorithm);
        if (supportedAlgorithms == null){
            throw new NotSupportedException(algorithm);
        }
        if (!supportedAlgorithms.matchesPublic(key)){
            throw new NotSupportedException("Can not verify data with key of type " + key.getClass().getName());
        }
        return supportedAlgorithms.verify(key, data, signature);
    }
}
