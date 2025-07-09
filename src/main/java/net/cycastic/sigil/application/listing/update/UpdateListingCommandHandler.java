package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class UpdateListingCommandHandler implements Command.Handler<UpdateListingCommand, @Null Object> {
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;

    @Override
    @Transactional
    public @Null Object handle(UpdateListingCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);
        var listing = listingRepository.findByPartitionAndListingPath(partitionService.getPartition(), command.getPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        listing.setListingPath(command.getNewPath());
        listing.setUpdatedAt(OffsetDateTime.now());

        listingRepository.save(listing);
        return null;
    }
}
