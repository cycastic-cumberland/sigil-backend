package net.cycastic.sigil.service.encryption;

import lombok.*;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class AEADDecryptionContext implements AutoCloseable{
    @Getter
    private final List<AEADEncryptedChunk> chunks;
    private final byte[] checksum;
    @Getter
    private final SecretKey secretKey;
    private final Path decryptionPath;
    private final OutputStream outputStream;

    @SneakyThrows
    public AEADDecryptionContext(List<AEADEncryptedChunk> chunks, byte[] checksum, SecretKey secretKey, Path decryptionPath) {
        this.chunks = chunks;
        this.checksum = checksum;
        this.secretKey = secretKey;
        this.decryptionPath = decryptionPath;
        this.outputStream = Files.newOutputStream(decryptionPath, StandardOpenOption.CREATE_NEW);
    }

    public void write(InputStream stream) throws IOException {
        stream.transferTo(outputStream);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        byte[] checksum;
        try (var inputStream = Files.newInputStream(decryptionPath, StandardOpenOption.READ)){
            checksum = SlimCryptographicUtilities.digestSha256(inputStream);
        }

        if (!Arrays.equals(this.checksum, checksum)){
            throw new IllegalStateException("File content corrupted");
        }
    }
}
