package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CreateAttachmentListingCommandHandler implements Command.Handler<CreateAttachmentListingCommand, AttachmentPresignedDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final TenantRepository tenantRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public AttachmentPresignedDto handle(CreateAttachmentListingCommand command) {
        var project = tenantRepository.findById(loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var path = command.getPath();
        var incompleteAttachment = listingService.saveTemporaryAttachment(project,
                path,
                command.getMimeType() != null ? command.getMimeType() : ApplicationUtilities.getMimeType(command.getPath()));

        var uploadUrl = storageProvider.getBucket(incompleteAttachment.getBucketName())
                .generatePresignedUploadPath(incompleteAttachment.getObjectKey(),
                        path,
                        OffsetDateTime.now().plusMinutes(2),
                        command.getContentLength(),
                        command.getKeyMd5());
        return AttachmentPresignedDto.builder()
                .id(incompleteAttachment.getId())
                .url(uploadUrl)
                .build();
    }
}
