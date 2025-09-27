package net.cycastic.sigil.application.listing.attachment.replace;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.DeferrableStorageProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverwriteAttachmentListingCommandHandler implements Command.Handler<OverwriteAttachmentListingCommand, Void> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;
    private final StorageProvider storageProvider;
    private final DeferrableStorageProvider deferrableStorageProvider;
    private final PartitionService partitionService;
    private final TenantRepository tenantRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(OverwriteAttachmentListingCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);

        var partition = partitionService.getPartition();
        if (!loggedUserAccessor.isAdmin() &&
                !partition.getPartitionType().equals(PartitionType.GENERIC)){
            throw RequestException.withExceptionCode("C403T003");
        }
        var sourceListing = listingRepository.findByPartitionAndListingPathForUpdate(partition, command.getSourcePath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var destinationListing = listingRepository.findByPartitionAndListingPath(partition, command.getDestinationPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var source = attachmentListingRepository.findByListing(sourceListing);
        var destination = attachmentListingRepository.findAttachmentListingForUpdate(destinationListing)
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        if (!source.isUploadCompleted() || !destination.isUploadCompleted()){
            throw new RequestException(400, "Attachment is uploading or corrupted");
        }
        destination.setUploadCompleted(false);
        attachmentListingRepository.save(destination);

        var project = tenantRepository.findByAttachmentListing(source)
                .orElseThrow(() -> new RequestException(404, "User not found"));
        sourceListing.setListingPath(destinationListing.getListingPath());
        var destSize = storageProvider.getBucket(destination.getBucketName()).getObjectSize(destination.getObjectKey());
        project.setAccumulatedAttachmentStorageUsage(project.getAccumulatedAttachmentStorageUsage() - destSize);
        deferrableStorageProvider.getBucket(destination.getBucketName()).deleteFile(destination.getObjectKey());

        tenantRepository.save(project);
        attachmentListingRepository.delete(destination);
        listingRepository.delete(destinationListing);
        listingRepository.save(sourceListing);

        return null;
    }
}
