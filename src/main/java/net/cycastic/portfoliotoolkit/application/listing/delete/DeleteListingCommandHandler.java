package net.cycastic.portfoliotoolkit.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.DecimalListingRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.TextListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteListingCommandHandler implements Command.Handler<DeleteListingCommand, @Null Object> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteListingCommandHandler.class);

    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final AttachmentListingRepository attachmentListingRepository;
    private final TextListingRepository textListingRepository;
    private final DecimalListingRepository decimalListingRepository;
    private final ProjectRepository projectRepository;
    private final StorageProvider storageProvider;

    @Override
    @Transactional
    public @Null Object handle(DeleteListingCommand command) {
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var listing = listingRepository.findByProjectAndListingPath(project, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));

        if (!listing.getProject().getId().equals(loggedUserAccessor.getProjectId())){
            throw new ForbiddenException();
        }

        // TODO: soft deletion
        switch (listing.getType()){
            case TEXT -> {
                textListingRepository.removeByListing(listing);
            }
            case DECIMAL -> {
                decimalListingRepository.removeByListing(listing);
            }
            case ATTACHMENT -> {
                var attachment = listing.getAttachmentListing();
                try {
                    storageProvider.getBucket(attachment.getBucketName()).deleteFile(attachment.getObjectKey());
                } catch (Exception e){
                    logger.error("Failed to delete attachment", e);
                    throw new RequestException(500, "Failed to delete attachment");
                }
                attachmentListingRepository.delete(attachment);
            }
        }
        listingRepository.delete(listing);
        return null;
    }
}
