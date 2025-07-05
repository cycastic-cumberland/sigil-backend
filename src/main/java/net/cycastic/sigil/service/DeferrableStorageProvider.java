package net.cycastic.sigil.service;

import jakarta.validation.constraints.NotNull;

public interface DeferrableStorageProvider {
    interface BucketProvider {
        void deleteFile(@NotNull String fileKey);
        void copyFile(@NotNull String sourceFileKey, @NotNull String destinationFileKey);
    }

    BucketProvider getBucket(@NotNull String bucketName);
}
