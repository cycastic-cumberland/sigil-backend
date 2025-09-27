package net.cycastic.sigil.service;

import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

public interface StorageProvider {
    interface BucketProvider {
        @NotNull String generatePresignedUploadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration, long objectLength, byte[] encryptionKeyMd5Checksum);
        @NotNull String generatePresignedDownloadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration, @Nullable String encryptionKeyMd5Checksum);
        void upload(@NotNull String fileKey, @NotNull String contentType, long contentLength, Supplier<InputStream> streamSupplier, @Nullable byte[] decryptionKey);
        InputStream download(@NotNull String fileKey, @NotNull String fileName, @Nullable byte[] decryptionKey);
        boolean exists(@NotNull String fileKey);
        long getObjectSize(@NotNull String fileKey);
        void deleteFile(@NotNull String fileKey);
        void copyFile(@NotNull String sourceFileKey, @NotNull String destinationFileKey);
    }

    BucketProvider getBucket(@NotNull String bucketName);
}
