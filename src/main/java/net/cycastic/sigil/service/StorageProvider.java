package net.cycastic.sigil.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.time.OffsetDateTime;

public interface StorageProvider {
    interface BucketProvider {
        @NotNull String generatePresignedUploadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration, long objectLength, @Nullable String encryptionKeyMd5Checksum);
        @NotNull String generatePresignedDownloadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration, @Nullable String encryptionKeyMd5Checksum);
        InputStream download(@NotNull String fileKey, @NotNull String fileName, @Nullable byte[] decryptionKey);
        boolean exists(@NotNull String fileKey);
        long getObjectSize(@NotNull String fileKey);
        void deleteFile(@NotNull String fileKey);
        void copyFile(@NotNull String sourceFileKey, @NotNull String destinationFileKey);
    }

    BucketProvider getBucket(@NotNull String bucketName);
}
