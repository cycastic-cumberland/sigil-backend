package net.cycastic.portfoliotoolkit.application.listing.attachment.replace;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.DeferrableStorageProvider;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverwriteAttachmentListingCommandHandler implements Command.Handler<OverwriteAttachmentListingCommand, @Null Object> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final StorageProvider storageProvider;
    private final DeferrableStorageProvider deferrableStorageProvider;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public @Null Object handle(OverwriteAttachmentListingCommand command) {
        var sourceListing = listingRepository.findByProject_IdAndListingPathForUpdate(loggedUserAccessor.getProjectId(), command.getSourcePath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var destinationListing = listingRepository.findByProject_IdAndListingPath(loggedUserAccessor.getProjectId(), command.getDestinationPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var source = attachmentListingRepository.findByListing(sourceListing);
        var destination = attachmentListingRepository.findAttachmentListingForUpdate(destinationListing)
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        if (!source.isUploadCompleted() || !destination.isUploadCompleted()){
            throw new RequestException(400, "Attachment is uploading or corrupted");
        }
        destination.setUploadCompleted(false);
        attachmentListingRepository.save(destination);

        var user = userRepository.findByAttachmentListing(source)
                .orElseThrow(() -> new RequestException(404, "User not found"));
        sourceListing.setListingPath(destinationListing.getListingPath());
        var destSize = storageProvider.getBucket(destination.getBucketName()).getObjectSize(destination.getObjectKey());
        user.setAccumulatedAttachmentStorageUsage(user.getAccumulatedAttachmentStorageUsage() - destSize);
        deferrableStorageProvider.getBucket(destination.getBucketName()).deleteFile(destination.getObjectKey());

        userRepository.save(user);
        attachmentListingRepository.delete(destination);
        listingRepository.delete(destinationListing);
        listingRepository.save(sourceListing);

        return null;
    }
}
