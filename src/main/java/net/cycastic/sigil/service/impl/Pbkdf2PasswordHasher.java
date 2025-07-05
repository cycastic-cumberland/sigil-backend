package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.auth.Pbkdf2Configuration;
import net.cycastic.sigil.service.PasswordHasher;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import java.nio.ByteBuffer;

@Lazy
@Service
@RequiredArgsConstructor
public class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final byte[] IDENTIFIER;
    private static final int KEY_LENGTH_BITS;
    private static final int SALT_SIZE;
    private static final SecureRandom RANDOM;

    private final Pbkdf2Configuration configuration;

    static {
        IDENTIFIER = "P2".getBytes(StandardCharsets.UTF_8);
        assert IDENTIFIER.length == 2;
        KEY_LENGTH_BITS = 256;
        SALT_SIZE = 16;
        RANDOM = new SecureRandom();
    }

    @Override
    public String hash(String password) {
        var salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);

        var iterations = configuration.getIterations();
        var hash = pbkdf2(password.toCharArray(), salt, iterations);
        var buffer = ByteBuffer.allocate(IDENTIFIER.length + Integer.BYTES + SALT_SIZE + hash.length);
        buffer.put(IDENTIFIER);
        buffer.putInt(iterations);
        buffer.put(salt);
        buffer.put(hash);

        return base64(buffer.array());
    }

    @Override
    public boolean verify(String input, String hashedPassword) {
        var digest = base64Decode(hashedPassword);
        if (digest.length <= IDENTIFIER.length + Integer.BYTES + SALT_SIZE) {
            return false;
        }


        if (!Arrays.equals(digest, 0, IDENTIFIER.length, IDENTIFIER, 0, IDENTIFIER.length)){
            var format = new String(digest, 0, IDENTIFIER.length, StandardCharsets.UTF_8);
            throw new IllegalStateException(String.format("Unknown hash format: %s", format));
        }

        int iterations;
        {
            var bytes = ByteBuffer.allocate(Integer.BYTES);
            bytes.put(digest, IDENTIFIER.length, Integer.BYTES);
            bytes.position(0);
            iterations = bytes.getInt();
        }
        var salt = Arrays.copyOfRange(digest,
                IDENTIFIER.length + Integer.BYTES,
                IDENTIFIER.length + Integer.BYTES + SALT_SIZE);
        var expectedHash = Arrays.copyOfRange(digest,
                IDENTIFIER.length + Integer.BYTES + SALT_SIZE,
                digest.length);
        var actualHash = pbkdf2(input.toCharArray(), salt, iterations);

        return constantTimeEquals(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        var gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(PKCS5S2ParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, iterations);
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
