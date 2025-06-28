package net.cycastic.portfoliotoolkit.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class EphemeralInputStream extends InputStream {
    private final InputStream masterStream;
    private final Path filePath;

    public EphemeralInputStream(Path filePath) throws IOException {
        this.filePath = filePath;
        masterStream = Files.newInputStream(filePath, StandardOpenOption.READ);
    }

    @Override
    public int read() throws IOException {
        return masterStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return masterStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        masterStream.close();
        Files.delete(filePath);
    }

    @Override
    public int available() throws IOException {
        return masterStream.available();
    }
}
