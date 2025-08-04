package net.cycastic.sigil.application.listing.attachment.create;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import org.hibernate.StaleObjectStateException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteAttachmentUploadCommandHandler implements Command.Handler<CompleteAttachmentUploadCommand, Void> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingService listingService;

    @Override
    public Void handle(CompleteAttachmentUploadCommand command) {
        for (;;){
            var listing = attachmentListingRepository.findById(command.getId())
                    .orElseThrow(() -> new RequestException(404, "Listing not found"));
            try {
                listingService.markAttachmentUploadAsCompleted(listing, true);
                return null;
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e){
                if (!(e.getRootCause() instanceof StaleObjectStateException)){
                    throw e;
                }
            }
        }
    }
}
