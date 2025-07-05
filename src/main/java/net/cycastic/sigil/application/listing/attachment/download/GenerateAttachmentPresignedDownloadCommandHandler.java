package net.cycastic.sigil.application.listing.attachment.download;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.AttachmentPresignedDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class GenerateAttachmentPresignedDownloadCommandHandler implements Command.Handler<GenerateAttachmentPresignedDownloadCommand, AttachmentPresignedDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final AttachmentListingRepository attachmentListingRepository;
    private final StorageProvider storageProvider;

    private AttachmentPresignedDto handle(GenerateAttachmentPresignedDownloadCommand command, @NotNull Integer projectId){
        var project = tenantRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var listing = attachmentListingRepository.findByListing_TenantAndListing_ListingPath(project, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));

        var url = storageProvider.getBucket(listing.getBucketName())
                .generatePresignedDownloadPath(listing.getObjectKey(),
                        new File(command.getListingPath()).getName(),
                        OffsetDateTime.now().plusHours(6)); // TODO: Override this
        return AttachmentPresignedDto.builder()
                .id(listing.getId())
                .url(url)
                .build();
    }

    @Override
    public AttachmentPresignedDto handle(GenerateAttachmentPresignedDownloadCommand command) {
        return handle(command, loggedUserAccessor.getTenantId());
    }
}
