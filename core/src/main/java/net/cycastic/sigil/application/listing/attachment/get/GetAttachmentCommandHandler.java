package net.cycastic.sigil.application.listing.attachment.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.dto.listing.AttachmentDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAttachmentCommandHandler implements Command.Handler<GetAttachmentCommand, AttachmentDto> {
    private final AttachmentListingRepository attachmentListingRepository;
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;

    @Override
    public AttachmentDto handle(GetAttachmentCommand command) {
        var partition = partitionService.getPartition();
        var attachmentListing = attachmentListingRepository.findByListing_PartitionAndListing_ListingPath(partition, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        var listing = listingRepository.findByAttachmentListing(attachmentListing);

        return AttachmentDto.fromDomain(attachmentListing, listing);
    }
}
