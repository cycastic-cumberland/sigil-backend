package net.cycastic.sigil.service.impl;

import jakarta.transaction.NotSupportedException;
import lombok.*;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@Service
public class Argon2idPasswordHasher extends KeyDerivationFunction {
    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CipherConfigurations implements KeyDerivationFunction.Parameters {
        private int parallelism;
        private int memoryKb;
        private int iterations;

        public static CipherConfigurations getDefault(){
            return new CipherConfigurations(PARALLELISM, MEMORY_KB, ITERATIONS);
        }

        @Override
        public void encode(OutputStream stream) {
            var intBytes = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            writeInt(stream, intBytes, parallelism);
            writeInt(stream, intBytes, memoryKb);
            writeInt(stream, intBytes, iterations);
        }

        @Override
        public boolean isMinimallyViable() {
            return parallelism >= PARALLELISM && memoryKb >= MEMORY_KB && iterations >= ITERATIONS;
        }

        public static CipherConfigurations decode(InputStream stream){
            var intBytes = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            var parallelism = readInt(stream, intBytes);
            var memoryKb = readInt(stream, intBytes);
            var iterations = readInt(stream, intBytes);
            return CipherConfigurations.builder()
                    .parallelism(parallelism)
                    .memoryKb(memoryKb)
                    .iterations(iterations)
                    .build();
        }

    }
    public static final int PARALLELISM = 1;
    public static final int MEMORY_KB = 1 << 15;
    public static final int ITERATIONS = 3;
    private static final int HASH_SIZE = 32;
    private static final byte[] IDENTIFIER = "A2".getBytes(StandardCharsets.UTF_8);
    @Override
    public String getIdentifier() {
        return "argon2id";
    }

    @Override
    public CipherConfigurations getParameters(InputStream stream) {
        return CipherConfigurations.decode(stream);
    }

    @Override
    public CipherConfigurations getDefaultParameters() {
        return CipherConfigurations.getDefault();
    }

    @Override
    @SneakyThrows
    public KeyDerivationResult derive(byte[] ikm, byte[] salt, Parameters parameters) {
        if (!(parameters instanceof CipherConfigurations cfg)){
            throw new NotSupportedException(parameters.getClass().getName());
        }

        var hash = argon2id(HASH_SIZE, ikm, salt, cfg);

        return KeyDerivationResult.builder()
                .salt(salt)
                .parameters(cfg)
                .hash(hash)
                .build();
    }

    @SneakyThrows
    private static int readInt(InputStream stream, ByteBuffer intBytes){
        var bytes = stream.read(intBytes.array());
        assert bytes == intBytes.array().length;
        intBytes.position(0);
        return intBytes.getInt();
    }

    @SneakyThrows
    private static void writeInt(OutputStream stream, ByteBuffer intBytes, int value){
        intBytes.position(0);
        intBytes.putInt(value);
        stream.write(intBytes.array());
    }

    @SneakyThrows
    private static void readExactly(InputStream stream, byte[] array){
        var bytes = stream.read(array);
        assert bytes == array.length;
    }

    public static byte[] argon2id(int outputLength, byte[] ikm, byte[] salt, int parallelism, int memoryKb, int iterations){
        var params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withParallelism(parallelism)
                .withMemoryAsKB(memoryKb)
                .withIterations(iterations)
                .build();
        var gen = new Argon2BytesGenerator();
        gen.init(params);
        var hash = new byte[outputLength];
        gen.generateBytes(ikm, hash);
        return hash;
    }

    public static byte[] argon2id(int outputLength, byte[] ikm, byte[] salt, CipherConfigurations cipherConfigurations){
        return argon2id(outputLength, ikm, salt, cipherConfigurations.parallelism, cipherConfigurations.memoryKb, cipherConfigurations.iterations);
    }

    public static CipherConfigurations extractCipherConfigurations(InputStream stream){
        var intBytes = ByteBuffer.allocate(Integer.BYTES);
        var parallelism = readInt(stream, intBytes);
        var memoryKb = readInt(stream, intBytes);
        var iterations = readInt(stream, intBytes);
        return CipherConfigurations.builder()
                .parallelism(parallelism)
                .memoryKb(memoryKb)
                .iterations(iterations)
                .build();
    }

    public static void writeCipherConfiguration(OutputStream outputStream, CipherConfigurations cipherConfigurations){
        var intBytes = ByteBuffer.allocate(Integer.BYTES);
        writeInt(outputStream, intBytes, cipherConfigurations.getParallelism());
        writeInt(outputStream, intBytes, cipherConfigurations.getMemoryKb());
        writeInt(outputStream, intBytes, cipherConfigurations.getIterations());
    }
}
