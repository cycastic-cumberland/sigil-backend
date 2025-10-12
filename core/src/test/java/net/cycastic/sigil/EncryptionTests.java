package net.cycastic.sigil;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.FilesUtilities;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.encryption.AEADDecryptionContext;
import net.cycastic.sigil.service.encryption.AEADEncryptionContext;
import net.cycastic.sigil.service.encryption.AEADEncryptor;
import net.cycastic.sigil.service.impl.HashicorpVaultEncryptionProvider;
import net.cycastic.sigil.service.impl.SymmetricEncryptionProvider;
import net.cycastic.sigil.service.impl.encryption.AESGCMEncryptor;
import net.cycastic.sigil.service.impl.encryption.ChaCha20Poly1305Encryptor;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EncryptionTests {
    @FunctionalInterface
    private interface CreateEncryptor {
        AEADEncryptor create(@Nullable Executor executor, long chunkSize);
    }
    private final HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider;
    private final SymmetricEncryptionProvider symmetricEncryptionProvider;

    @Autowired
    public EncryptionTests(HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider, SymmetricEncryptionProvider symmetricEncryptionProvider) {
        this.hashicorpVaultEncryptionProvider = hashicorpVaultEncryptionProvider;
        this.symmetricEncryptionProvider = symmetricEncryptionProvider;
    }

    private static void testEncryptDecrypt(String text, EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider){
        var encrypted = encryptionProvider.encrypt(text);
        var decrypted = decryptionProvider.decrypt(encrypted);
        assertEquals(text, decrypted);
    }

    private static void testEncryptDecrypt(EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider){
        testEncryptDecrypt("Hello World!", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("Xin chào thế giới!", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("CXNljNhbX+PV88d5OvRSJfABfPtETSk8DHXGLjSPvuy031tXmHnrGpjEr4hXW31jHgzUvF+f1zNO9fO35BE7hdtkzDttuXAyHTeJw2tpFfcYPSapALIC3iL4jGZnjzPZasl4WGS3wQcc5/KPfgx7WTFMHkAxsMiNIjMB5RhCZ5FIjPv6agei7CH0KHWUjl+YbZwXwgoWhao+EmotrqvKMTZgeRP4QUT1aq3m5d4yr1iwLj8LgRhFCZQEZPmY3L6SdIxKBZtmNgZRe0Dsj3N7wtlmx5SH0RLtzoaBQWMBqfQnExOkhsHAWdEfvZFcQJifWtAEPfuTacE2Vhoz3daP+tsZC2Od3rAAKTpe8w1E7es+gDxM1tQe6N1ppZxy7A4CJV7j9qunvFY5PhBxgrP5jxaaKyLBDJ8nz7Yh1hEaW3pwG5p9ONv3xowoU/hM+2KIYRMU0Ji5WEq/6eWaxxXkbp1+tQTXS+jCbBc/K0EpO+9uY5sAbvrLSYwVsZdE1IwL07zUqJwRh1AWtM+MPt0iS9aSoGRh6d6IULUYd/8wTt2eV7YyPvJGll7pMiFxgCZAppSLcO2EWBP4Dt75ls2fDnlSaZR8nsqD4ARmU5OSxO+Q9okzej1S7m556575pGKeOi22wuvgXKzAbRusI8ydHFGlIskRQW79rJdOAm8IgiCYvxQdILfKVmc5I1rt6TAEDdPvDh4t3LYKcHtrxjh4/Q3wypiwqjQrve5nF78P3gVDjkyxeIPRo2dd/9fVi1yYpkCjRycg+sLlbHdPRviIZc/+MIRQ4TtZl4v4KC+4qFOJBS5p+G6TPEkZa+AHjXHtQuS+h4WaYbvHEE/M3uMm5Kz1/BeBUIGK/resqfeFzo//EDGz6myTTiDeawGxAG6fgbw1D2EKoBO39vTZJYSQCPfQ51PCqjUHC9p79k5jH6NiuvX/lSHImycxaEmyjuqKStDJBei2V3WnVgVxjf5N79UITfFmMEDjbI9IEQHGqn5kerLUqZ+HYHFHyI5O9rVr+8kO65MTHWo6SzHLoq1lxEJ+SdT6oUW4NyXnyTUTTJBQCouko0JG7rbXO5N/dzzYgnC0+2J67cRm5zKa59J7+rEqseWrU2Ek11VYB2+cZQOe0tO7AhPKF+soEt94pc0bpjg2zswGrbn5z3JsovKRxMeZUBZaBOaI/tYSaMd+9uXg6Oti4QBd8t819WbBhDRX9k81tne0yFvaNGgCAnb2jWt4tLd9yUtEeMYBoiYpdArL3sIRSOaiOaWGjM7+V6fgRDmjxXGeKte6selB9E6VGTVQBsY6go0xy1eVAfhEz813/eb+eu3PyaBn8eLaWMqorXdSji8O85ljY+DdxLwYkg==", encryptionProvider, decryptionProvider);
    }

    @Test
    public void testHashicorpVaultTransitEncryption(){
        testEncryptDecrypt(hashicorpVaultEncryptionProvider, hashicorpVaultEncryptionProvider);
    }

    @Test
    public void testSymmetricEncryption(){
        testEncryptDecrypt(symmetricEncryptionProvider, symmetricEncryptionProvider);
    }

    @SneakyThrows
    private static void testChaCha20Poly1305Internal(Executor executor, CreateEncryptor createEncryptor){
        var root = FilesUtilities.getTempFile();
        Files.createDirectory(root);
        try {
            var encryptor = createEncryptor.create(executor, 2048);
            var key = encryptor.createKey();
            var plainTextFile = root.resolve("file.bin");
            try (var outputStream = Files.newOutputStream(plainTextFile, StandardOpenOption.CREATE_NEW)){
                var chunk = new byte[4096];
                for (var i = 0; i < 16; i++){
                    SlimCryptographicUtilities.generateRandom(chunk);
                    outputStream.write(chunk);
                }

                SlimCryptographicUtilities.generateRandom(chunk);
                outputStream.write(chunk, 0, 269); // random prime number
            }

            var totalLength = 16 * 4096 + 269;
            assertEquals(totalLength, Files.size(plainTextFile));
            var chunkCount = totalLength / 2048 + 1;

            var encryptedParts = root.resolve("encrypted");
            Files.createDirectory(encryptedParts);

            // Encryption
            var encryptionContext = new AEADEncryptionContext(key, plainTextFile, encryptedParts);
            encryptor.encrypt(encryptionContext);
            assertEquals(chunkCount, encryptionContext.getChunks().size());
            assertNotNull(encryptionContext.getChecksum());

            // Decryption
            var decrypted = root.resolve("decrypted.bin");
            try (var decryptionContext = new AEADDecryptionContext(encryptionContext.getChunks(), encryptionContext.getChecksum(), key, decrypted)){
                encryptor.decrypt(decryptionContext);
            }
        } finally {
            FilesUtilities.deleteRecursively(root);
        }
    }

    @RepeatedTest(10)
    public void testChaCha20Poly1305VirtualThreads(){
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            testChaCha20Poly1305Internal(executor, ChaCha20Poly1305Encryptor::new);
        }
    }

    @Test
    public void testChaCha20Poly1305SingleThread(){
        testChaCha20Poly1305Internal(null, ChaCha20Poly1305Encryptor::new);
    }

    @RepeatedTest(10)
    public void testAESGCMVirtualThreads(){
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            testChaCha20Poly1305Internal(executor, AESGCMEncryptor::new);
        }
    }

    @Test
    public void testAESGCMSingleThread(){
        testChaCha20Poly1305Internal(null, AESGCMEncryptor::new);
    }
}
