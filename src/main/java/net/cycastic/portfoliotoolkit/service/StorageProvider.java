package net.cycastic.portfoliotoolkit.service;

import jakarta.validation.constraints.NotNull;

import java.io.OutputStream;
import java.time.OffsetDateTime;

public interface StorageProvider {
    interface BucketProvider {
        @NotNull String generatePresignedUploadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration);
        @NotNull String generatePresignedDownloadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration);
        void downloadFile(@NotNull String fileKey, OutputStream stream);
        boolean exists(@NotNull String fileKey);
    }

    BucketProvider getBucket(@NotNull String bucketName);
}
