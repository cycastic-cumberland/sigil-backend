package net.cycastic.sigil.service.impl.storage;

import lombok.SneakyThrows;
import net.cycastic.sigil.service.storage.CompressUtility;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Path;

public class ZipCompressUtility implements CompressUtility {
    public static final ZipCompressUtility INSTANCE = new ZipCompressUtility();

    @Override
    @SneakyThrows
    public void compressFolder(Path inputPath, OutputStream outputStream) {
        ZipUtil.pack(inputPath.toFile(), outputStream);
    }

    @Override
    @SneakyThrows
    public void decompressFolder(Path outputPath, InputStream inputStream) {
        ZipUtil.unpack(inputStream, outputPath.toFile());
    }
}
