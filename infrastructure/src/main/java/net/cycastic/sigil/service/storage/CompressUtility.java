package net.cycastic.sigil.service.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface CompressUtility {
    void compressFolder(Path inputPath, OutputStream outputStream);
    void decompressFolder(Path outputPath, InputStream inputStream);
}
