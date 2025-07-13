package net.cycastic.sigil.service.auth;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.CryptographicUtilities;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class KeyDerivationFunction implements PasswordEncoder {
    public static final int SALT_SIZE = 16;

    public interface Parameters {
        void encode(OutputStream stream);

        @SneakyThrows
        default byte[] encode(){
            try (var stream = new ByteArrayOutputStream()){
                encode(stream);
                return stream.toByteArray();
            }
        }
    }

    public interface KeyDerivationSettings {
        byte[] getSalt();
        Parameters getParameters();
    }

    @Data
    @Builder
    public static class KeyDerivationResult implements KeyDerivationSettings {
        byte[] salt;
        Parameters parameters;
        byte[] hash;
    }

    public abstract String getIdentifier();

    public abstract Parameters getParameters(InputStream stream);

    public abstract Parameters getDefaultParameters();

    public abstract KeyDerivationResult derive(byte[] ikm, byte[] salt, Parameters parameters);

    public String encodeSettings(KeyDerivationSettings settings){
        var encoder = Base64.getEncoder();
        return getIdentifier() + "$" + encoder.encodeToString(settings.getSalt()) + "$"
                + encoder.encodeToString(settings.getParameters().encode());
    }

    @SneakyThrows
    public KeyDerivationSettings decodeSettings(String encoded){
        var parts = encoded.split("\\$");
        if (parts.length < 3){
            throw new IllegalStateException("Can not decode contents");
        }
        if (!parts[0].equals(getIdentifier())){
            throw new IllegalStateException("Unsupported function: " + parts[0]);
        }

        var decoder = Base64.getDecoder();
        final var salt = decoder.decode(parts[1]);
        final Parameters parameters;
        try (var stream = new ByteArrayInputStream(decoder.decode(parts[2]))){
            parameters = getParameters(stream);
        }
        if (parts.length == 4){
            var hash = decoder.decode(parts[3]);
            return KeyDerivationResult.builder()
                    .salt(salt)
                    .parameters(parameters)
                    .hash(hash)
                    .build();
        }

        return new KeyDerivationSettings() {
            @Override
            public byte[] getSalt() {
                return salt;
            }

            @Override
            public Parameters getParameters() {
                return parameters;
            }
        };
    }

    public String encode(byte[] ikm, byte[] salt, Parameters parameters){
        var result = derive(ikm, salt, parameters);
        var encoder = Base64.getEncoder();
        return String.join("$", encodeSettings(result), encoder.encodeToString(result.getHash()));
    }

    public String encode(byte[] ikm, Parameters parameters){
        var salt = new byte[SALT_SIZE];
        CryptographicUtilities.generateRandom(salt);
        return encode(ikm, salt, parameters);
    }

    public String encode(byte[] ikm){
        return encode(ikm, getDefaultParameters());
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return encode(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
    }

    public boolean canDecode(String encoded){
        return encoded.startsWith(getIdentifier() + "$");
    }

    @SneakyThrows
    public KeyDerivationResult decode(String encoded){
        var settings = decodeSettings(encoded);
        if (settings instanceof KeyDerivationResult result){
            return result;
        }

        throw new IllegalStateException("Can not decode contents");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        var expected = decode(encodedPassword);
        var actual = derive(rawPassword.toString().getBytes(StandardCharsets.UTF_8), expected.getSalt(), expected.getParameters());
        return CryptographicUtilities.constantTimeEquals(expected.hash, actual.hash);
    }
}
