package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class UpdateListingCommandHandler implements Command.Handler<UpdateListingCommand, Void> {
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(UpdateListingCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.WRITE);
        var partition = partitionService.getPartition();
        if (!loggedUserAccessor.isAdmin() && !partition.getPartitionType().equals(PartitionType.GENERIC)){
            throw RequestException.withExceptionCode("C403T003");
        }
        var listing = listingRepository.findByPartitionAndListingPath(partition, command.getPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        listing.setListingPath(command.getNewPath());
        listing.setUpdatedAt(OffsetDateTime.now());

        listingRepository.save(listing);
        return null;
    }
}
