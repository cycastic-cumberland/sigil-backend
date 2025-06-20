package net.cycastic.portfoliotoolkit.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteAttachmentUploadCommandHandler implements Command.Handler<CompleteAttachmentUploadCommand, @Null Object> {
    private final StorageProvider storageProvider;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    @Override
    public Object handle(CompleteAttachmentUploadCommand command) {
        var listing = attachmentListingRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        if (!listing.getListing().getProject().getId().equals(loggedUserAccessor.getProjectId())){
            throw new RequestException(404, "Invalid request");
        }
        if (listing.isUploadCompleted()){
            return null;
        }

        if (!storageProvider.getBucket(listing.getBucketName()).exists(listing.getObjectKey())){
            throw new RequestException(400, "Object has not been uploaded yet");
        }
        listing.setUploadCompleted(true);
        attachmentListingRepository.save(listing);
        return null;
    }
}
