package net.cycastic.sigil.service.encryption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AEADEncryptionContext {
    private final List<AEADEncryptedChunk> chunks = new ArrayList<>();
    private final SecretKey secretKey;
    private final Path inputPath;
    private final Path baseOutputPath;

    @Setter
    private byte[] checksum;
}
