package net.cycastic.sigil.service.impl.encryption;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.AggregatedException;
import net.cycastic.sigil.domain.FilesUtilities;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.service.encryption.AEADDecryptionContext;
import net.cycastic.sigil.service.encryption.AEADEncryptedChunk;
import net.cycastic.sigil.service.encryption.AEADEncryptionContext;
import net.cycastic.sigil.service.encryption.AEADEncryptor;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public abstract class BaseAEADParallelChunkEncryptor implements AEADEncryptor {
    private final long chunkSize;
    @Nullable
    private final Executor executor;
    protected BaseAEADParallelChunkEncryptor(@Nullable Executor executor, long chunkSize){
        if (chunkSize <= 1024){
            throw new IllegalArgumentException("chunkSize");
        }

        this.executor = executor;
        this.chunkSize = chunkSize;
    }
    protected abstract byte[] decryptChunk(byte[] cipherText, SecretKey secretKey, IvParameterSpec iv);

    protected abstract byte[] encryptChunk(byte[] plainText, SecretKey secretKey, IvParameterSpec iv);

    @SneakyThrows
    private void decryptChunk(int index, Path rootTemp, Path[] paths, AEADDecryptionContext context){
        var chunk = context.getChunks().get(index);
        var output = rootTemp.resolve(UUID.randomUUID().toString());
        var iv = new IvParameterSpec(Base64.getDecoder().decode(chunk.getIv()));

        var encrypted = Files.readAllBytes(Path.of(chunk.getPath()));
        var decrypted = decryptChunk(encrypted, context.getSecretKey(), iv);
        Files.write(output, decrypted, StandardOpenOption.CREATE_NEW);
        paths[index] = output;
    }

    @SneakyThrows
    private void encryptChunk(int index, MappedByteBuffer input, AEADEncryptedChunk[] chunks, AEADEncryptionContext context){
        var ivByte = new byte[12];
        SlimCryptographicUtilities.generateRandom(ivByte);
        var iv = new IvParameterSpec(ivByte);
        var output = context.getBaseOutputPath().resolve(UUID.randomUUID().toString());

        var plain = new byte[input.remaining()];
        input.get(plain);
        var encrypted = encryptChunk(plain, context.getSecretKey(), iv);
        Files.write(output, encrypted, StandardOpenOption.CREATE_NEW);

        chunks[index] = AEADEncryptedChunk.builder()
                .iv(Base64.getEncoder().encodeToString(ivByte))
                .path(output.toString())
                .build();
    }

    @Override
    @SneakyThrows
    public void encrypt(AEADEncryptionContext context) {
        var totalFileSize = Files.size(context.getInputPath());
        var chunkCount = (int)(totalFileSize / chunkSize + 1);
        var chunks = new AEADEncryptedChunk[chunkCount];
        var latch = new CountDownLatch(chunkCount);
        final var exceptions = new ConcurrentLinkedQueue<Throwable>();
        try (var inputStream = Files.newInputStream(context.getInputPath(), StandardOpenOption.READ)){
            var plainTextChecksum = SlimCryptographicUtilities.digestSha256(inputStream);
            context.setChecksum(plainTextChecksum);
        }
        try (var fc = FileChannel.open(context.getInputPath(), StandardOpenOption.READ)){
            for (var i = 0; i < chunkCount; i++){
                final var index = i;
                final var segment = fc.map(FileChannel.MapMode.READ_ONLY,
                        i * chunkSize,
                        i == chunkCount - 1 ? totalFileSize - (i * chunkSize) : chunkSize);
                Runnable runnable = () -> {
                    try {
                        encryptChunk(index, segment, chunks, context);
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                };
                if (executor != null && chunkCount > 1) {
                    executor.execute(runnable);
                } else {
                    runnable.run();
                }
            }

            latch.await();
        }
        if (!exceptions.isEmpty()){
            AggregatedException.doThrow(exceptions.stream().toList());
        }

        for (var chunk : chunks){
            context.getChunks().add(chunk);
        }
    }

    @Override
    @SneakyThrows
    public void decrypt(AEADDecryptionContext context) {
        final var chunksDir = FilesUtilities.getTempFile();
        try {
            final var chunkCount = context.getChunks().size();
            final var paths = new Path[chunkCount];
            final var exceptions = new ConcurrentLinkedQueue<Throwable>();
            Files.createDirectory(chunksDir);
            var latch = new CountDownLatch(chunkCount);
            for (var i = 0; i < chunkCount; i++){
                final var index = i;
                Runnable runnable = () -> {
                    try {
                        decryptChunk(index, chunksDir, paths, context);
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                };
                if (executor != null && chunkCount > 1) {
                    executor.execute(runnable);
                } else {
                    runnable.run();
                }
            }

            latch.await();
            if (!exceptions.isEmpty()){
                AggregatedException.doThrow(exceptions.stream().toList());
            }

            for (var path : paths){
                try (var inputStream = Files.newInputStream(path, StandardOpenOption.READ)){
                    context.write(inputStream);
                }
            }
        } finally {
            FilesUtilities.deleteRecursively(chunksDir);
        }
    }
}
