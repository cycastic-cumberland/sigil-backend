package net.cycastic.sigil;

import net.cycastic.sigil.domain.FilesUtilities;
import net.cycastic.sigil.service.storage.CompressUtility;
import net.cycastic.sigil.service.impl.storage.ZipCompressUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CompressionTests {
    private static void testCompressionFull(CompressUtility compressUtility){
        try {
            var tempDir = FilesUtilities.getTempFile();
            Files.createDirectories(tempDir);
            createRandomFiles(tempDir);
            var originalChecksums = collectChecksums(tempDir);
            var outputStream = new ByteArrayOutputStream();
            compressUtility.compressFolder(tempDir, outputStream);
            outputStream.close();
            var archive = outputStream.toByteArray();
            var destDir = FilesUtilities.getTempFile();
            var inputStream = new ByteArrayInputStream(archive);
            compressUtility.decompressFolder(destDir, inputStream);
            inputStream.close();
            var decompressedChecksums = collectChecksums(destDir);
            assertEquals(originalChecksums.size(), decompressedChecksums.size(), "File count mismatch");
            for (var entry : originalChecksums.entrySet()) {
                String relPath = entry.getKey();
                assertTrue(decompressedChecksums.containsKey(relPath), "Missing file: " + relPath);
                assertEquals(entry.getValue(), decompressedChecksums.get(relPath), "Checksum mismatch for: " + relPath);
            }
            FilesUtilities.deleteRecursively(tempDir);
            FilesUtilities.deleteRecursively(destDir);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void createRandomFiles(Path baseDir) throws IOException {
        var rnd = new Random(42);
        Files.createDirectories(baseDir.resolve("sub1/sub2"));
        Files.createDirectories(baseDir.resolve("emptydir"));
        Files.write(baseDir.resolve("file1.txt"), "Hello, world!\n".getBytes());
        byte[] buf2 = new byte[1024];
        rnd.nextBytes(buf2);
        Files.write(baseDir.resolve("file2.bin"), buf2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("Line ").append(i).append('\n');
        }
        Files.write(baseDir.resolve("sub1/file3.txt"), sb.toString().getBytes());
        var buf4 = new byte[2048];
        rnd.nextBytes(buf4);
        Files.write(baseDir.resolve("sub1/sub2/file4.bin"), buf4);
    }

    private static String checksum(Path file) throws IOException, NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var in = Files.newInputStream(file)) {
            var buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                digest.update(buf, 0, n);
            }
        }
        var hash = digest.digest();
        var hex = new StringBuilder();
        for (var b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static Map<String, String> collectChecksums(Path root) throws IOException {
        var checksums = new HashMap<String, String>();
        try (var stream = Files.walk(root)){
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String rel = root.relativize(path).toString().replace('\\','/');
                            checksums.put(rel, checksum(path));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return checksums;
    }

    @Test
    public void testZipCompression(){
        testCompressionFull(ZipCompressUtility.INSTANCE);
    }
}
