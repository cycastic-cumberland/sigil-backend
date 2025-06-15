package net.cycastic.portfoliotoolkit.mail.template;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.net.URLConnection;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateMailTemplateFromUploadCommandHandler implements Command.Handler<CreateMailTemplateFromUploadCommand, AttachmentPresignedUploadDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    private static String getMimeType(String fileName) {
        return URLConnection.guessContentTypeFromName(fileName);
    }

    @Override
    public AttachmentPresignedUploadDto handle(CreateMailTemplateFromUploadCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var path = Objects.requireNonNullElseGet(command.getListingPath(), () -> UUID.randomUUID().toString());
        if (attachmentListingRepository.existsByListing_Project_AndUploadCompleted(project, false)){
            throw new RequestException(400, "There are incomplete attachment uploads");
        }

        var incompleteAttachment = listingService.saveAttachment(project,
                path,
                command.getMimeType() == null ? null : getMimeType(command.getMimeType()));

        var uploadUrl = storageProvider.getBucket(incompleteAttachment.getBucketName())
                .generatePresignedUploadPath(incompleteAttachment.getObjectKey(),
                        command.getFileName(),
                        OffsetDateTime.now().plusHours(6)); // TODO: Override this
        return AttachmentPresignedUploadDto.builder()
                .id(incompleteAttachment.getId())
                .url(uploadUrl)
                .build();
    }
}
