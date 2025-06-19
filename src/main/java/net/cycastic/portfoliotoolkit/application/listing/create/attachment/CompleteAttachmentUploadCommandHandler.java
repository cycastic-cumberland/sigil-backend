package net.cycastic.portfoliotoolkit.application.listing.create.attachment;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteAttachmentUploadCommandHandler implements Command.Handler<CompleteAttachmentUploadCommand, @Null Object> {
    private final StorageProvider storageProvider;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    @Override
    public Object handle(CompleteAttachmentUploadCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        if (!loggedUserAccessor.isAdmin() && !project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }
        var listing = attachmentListingRepository.findAttachmentListingByListing_Project(project)
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        if (listing.isUploadCompleted()){
            return null;
        }

        if (!storageProvider.getBucket(listing.getBucketName()).exists(listing.getObjectKey())){
            throw new RequestException(400, "Object has not been oploaded yet");
        }
        listing.setUploadCompleted(true);
        attachmentListingRepository.save(listing);
        return null;
    }
}
