package net.cycastic.portfoliotoolkit.application.listing.create.attachment;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CreateAttachmentListingCommandHandler implements Command.Handler<CreateAttachmentListingCommand, AttachmentPresignedUploadDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    @Override
    public AttachmentPresignedUploadDto handle(CreateAttachmentListingCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        if (!loggedUserAccessor.isAdmin() && !project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }
        var path = command.getPath();
        if (attachmentListingRepository.existsByListing_Project_AndUploadCompleted(project, false)){
            throw new RequestException(400, "There are incomplete attachment uploads");
        }

        var incompleteAttachment = listingService.saveAttachment(project,
                path,
                command.getMimeType() == null ? null : ApplicationUtilities.getMimeType(command.getMimeType()));

        var uploadUrl = storageProvider.getBucket(incompleteAttachment.getBucketName())
                .generatePresignedUploadPath(incompleteAttachment.getObjectKey(),
                        path,
                        OffsetDateTime.now().plusHours(6)); // TODO: Override this
        return AttachmentPresignedUploadDto.builder()
                .id(incompleteAttachment.getId())
                .url(uploadUrl)
                .build();
    }
}
