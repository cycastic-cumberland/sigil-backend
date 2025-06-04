package net.cycastic.portfoliotoolkit.service;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class Pbkdf2PasswordHasher implements PasswordHasher{
    private static final int ITERATIONS = 310_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_SIZE = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String hash(String password) {
        var salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);

        var hash = pbkdf2(password.toCharArray(), salt);
        var digest = new byte[SALT_SIZE + hash.length];
        System.arraycopy(salt, 0, digest, 0, SALT_SIZE);
        System.arraycopy(hash, 0, digest, SALT_SIZE, hash.length);
        return base64(digest);
    }

    @Override
    public boolean verify(String input, String hashedPassword) {
        var digest = base64Decode(hashedPassword);
        if (digest.length <= SALT_SIZE) return false;

        var salt = Arrays.copyOfRange(digest, 0, SALT_SIZE);
        var expectedHash = Arrays.copyOfRange(digest, SALT_SIZE, digest.length);
        var actualHash = pbkdf2(input.toCharArray(), salt);

        return constantTimeEquals(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(PKCS5S2ParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, ITERATIONS);
        return ((KeyParameter) gen.generateDerivedParameters(KEY_LENGTH_BITS)).getKey();
    }

    private static String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        var result = 0;
        for (var i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
