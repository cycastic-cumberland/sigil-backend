package net.cycastic.portfoliotoolkit.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.listing.AttachmentListing;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LimitProvider;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompleteAttachmentUploadCommandHandler implements Command.Handler<CompleteAttachmentUploadCommand, @Null Object> {
    private static final Logger logger = LoggerFactory.getLogger(CompleteAttachmentUploadCommandHandler.class);
    private final StorageProvider storageProvider;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;
    private final UserRepository userRepository;
    private final LimitProvider limitProvider;

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
        if (!listing.getListing().getProject().getId().equals(loggedUserAccessor.getProjectId())){
            throw new RequestException(404, "Invalid request");
        }
        if (listing.isUploadCompleted()){
            return null;
        }

        long size;
        try {
            size = storageProvider.getBucket(listing.getBucketName()).getObjectSize(listing.getObjectKey());
        } catch (RequestException e){
            if (e.getResponseCode() == 404){
                throw new RequestException(400, "Object has not been uploaded yet");
            }

            throw e;
        }

        var user = userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var limit = limitProvider.extractUsageDetails(user);
        if (limit.getPerAttachmentSize() != null && size > limit.getPerAttachmentSize()){
            logger.error("File is larger than permitted limit. Size: {} byte(s), limit: {} byte(s)",
                    size, limit.getPerAttachmentSize());
            throw new RequestException(413, "File is larger than permitted limit");
        }
        var acc = user.getAccumulatedAttachmentStorageUsage() + size;
        if (limit.getAllAttachmentSize() != null && acc > limit.getAllAttachmentSize()){
            throw new RequestException(413, "Accumulated storage usage exceeded");
        }
        completeAttachmentUpload(listing);

        user.setAccumulatedAttachmentStorageUsage(acc);
        userRepository.save(user);
        return null;
    }
}
