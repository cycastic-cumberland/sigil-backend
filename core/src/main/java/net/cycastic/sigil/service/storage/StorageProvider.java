package net.cycastic.sigil.service.storage;

import jakarta.validation.constraints.NotNull;

public interface StorageProvider {
    BucketProvider getBucket(@NotNull String bucketName);
}
