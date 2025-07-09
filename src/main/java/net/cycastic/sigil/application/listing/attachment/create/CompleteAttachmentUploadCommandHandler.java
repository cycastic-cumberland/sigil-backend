package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.service.LimitProvider;
import net.cycastic.sigil.service.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteAttachmentUploadCommandHandler implements Command.Handler<CompleteAttachmentUploadCommand, @Null Object> {
    private static final Logger logger = LoggerFactory.getLogger(CompleteAttachmentUploadCommandHandler.class);
    private final StorageProvider storageProvider;
    private final AttachmentListingRepository attachmentListingRepository;
    private final TenantRepository tenantRepository;
    private final LimitProvider limitProvider;
    private final PartitionService partitionService;

    private void completeAttachmentUpload(@NotNull AttachmentListing listing){
        final var oldObjectKey = listing.getObjectKey();
        if (!oldObjectKey.startsWith(ListingService.TEMP_FILE_PREFIX)){
            logger.error("Attachment does not start with predefined prefix: {}", oldObjectKey);
            throw new RequestException(500, "Failed to mark attachment upload as completed");
        }

        var newObjectKey = oldObjectKey.substring(ListingService.TEMP_FILE_PREFIX.length());
        newObjectKey = ApplicationUtilities.shardObjectKey(newObjectKey);
        var bucket = storageProvider.getBucket(listing.getBucketName());
        bucket.copyFile(oldObjectKey, newObjectKey);
        listing.setObjectKey(newObjectKey);
        listing.setUploadCompleted(true);
        attachmentListingRepository.save(listing);
        bucket.deleteFile(oldObjectKey);
    }

    @Override
    @Transactional
    public Object handle(CompleteAttachmentUploadCommand command) {
        var listing = attachmentListingRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var tenant = tenantRepository.findByAttachmentListing(listing)
                .orElseThrow(() -> new RequestException(404, "Tenant not found"));

        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);

        if (listing.isUploadCompleted()){
            return null;
        }

        var size = listing.getContentLength();
        var limit = limitProvider.extractUsageDetails(tenant);
        if (limit.getPerAttachmentSize() != null && size > limit.getPerAttachmentSize()){
            logger.error("File is larger than permitted limit. Size: {} byte(s), limit: {} byte(s)",
                    size, limit.getPerAttachmentSize());
            throw new RequestException(413, "File is larger than permitted limit");
        }
        var acc = tenant.getAccumulatedAttachmentStorageUsage() + size;
        if (limit.getAllAttachmentSize() != null && acc > limit.getAllAttachmentSize()){
            throw new RequestException(413, "Accumulated storage usage exceeded");
        }
        completeAttachmentUpload(listing);

        tenant.setAccumulatedAttachmentStorageUsage(acc);
        tenantRepository.save(tenant);
        return null;
    }
}
