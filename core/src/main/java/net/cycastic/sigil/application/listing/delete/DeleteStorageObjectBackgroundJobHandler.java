package net.cycastic.sigil.application.listing.delete;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import net.cycastic.sigil.service.storage.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteStorageObjectBackgroundJobHandler implements BackgroundJobHandler<DeleteStorageObjectBackgroundJob> {
    private final StorageProvider storageProvider;

    @Override
    public void process(DeleteStorageObjectBackgroundJob data) {
        var bucket = storageProvider.getBucket(data.getBucketName());
        bucket.deleteFile(data.getObjectKey());
    }
}
