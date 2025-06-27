package net.cycastic.portfoliotoolkit.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.OutputStream;
import java.time.OffsetDateTime;

public interface StorageProvider {
    interface BucketProvider {
        @NotNull String generatePresignedUploadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration, long objectLength);
        @NotNull String generatePresignedDownloadPath(@NotNull String fileKey, @NotNull String fileName, @NotNull OffsetDateTime expiration);
        void downloadFile(@NotNull String fileKey, OutputStream stream);
        boolean exists(@NotNull String fileKey);
        long getObjectSize(@NotNull String fileKey);
        void deleteFile(@NotNull String fileKey);
        void copyFile(@NotNull String sourceFileKey, @NotNull String destinationFileKey);
    }

    BucketProvider getBucket(@NotNull String bucketName);
}
