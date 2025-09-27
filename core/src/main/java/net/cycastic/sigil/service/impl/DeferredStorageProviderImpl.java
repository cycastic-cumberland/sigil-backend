package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.DeferrableStorageProvider;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Lazy
@Component
@RequiredArgsConstructor
public class DeferredStorageProviderImpl implements DeferrableStorageProvider {
    private final StorageProvider storageProvider;
    private final TaskExecutor taskScheduler;

    @RequiredArgsConstructor
    public static class DeferredBucketProvider implements BucketProvider {
        private final TaskExecutor executor;
        private final StorageProvider.BucketProvider bucketProvider;

        @Override
        public void deleteFile(String fileKey) {
            executor.execute(() -> bucketProvider.deleteFile(fileKey));
        }

        @Override
        public void copyFile(String sourceFileKey, String destinationFileKey) {
            executor.execute(() -> bucketProvider.copyFile(sourceFileKey, destinationFileKey));
        }
    }

    @Override
    public BucketProvider getBucket(String bucketName) {
        var bucket = storageProvider.getBucket(bucketName);
        return new DeferredBucketProvider(taskScheduler, bucket);
    }
}
