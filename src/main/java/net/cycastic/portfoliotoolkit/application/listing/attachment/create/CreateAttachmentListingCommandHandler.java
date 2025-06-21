package net.cycastic.portfoliotoolkit.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CreateAttachmentListingCommandHandler implements Command.Handler<CreateAttachmentListingCommand, AttachmentPresignedDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    @Override
    public AttachmentPresignedDto handle(CreateAttachmentListingCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var path = command.getPath();
        if (attachmentListingRepository.existsByListing_Project_AndUploadCompleted(project, false)){
            throw new RequestException(400, "There are incomplete attachment uploads");
        }

        var incompleteAttachment = listingService.saveAttachment(project,
                path,
                command.getMimeType() == null ? null : ApplicationUtilities.getMimeType(command.getPath()));

        var uploadUrl = storageProvider.getBucket(incompleteAttachment.getBucketName())
                .generatePresignedUploadPath(incompleteAttachment.getObjectKey(),
                        path,
                        OffsetDateTime.now().plusHours(6)); // TODO: Override this
        return AttachmentPresignedDto.builder()
                .id(incompleteAttachment.getId())
                .url(uploadUrl)
                .build();
    }
}
