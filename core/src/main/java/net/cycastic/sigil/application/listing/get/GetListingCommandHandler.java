package net.cycastic.sigil.application.listing.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetListingCommandHandler implements Command.Handler<GetListingCommand, ListingDto> {
    private final ListingService listingService;
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;

    @Override
    public ListingDto handle(GetListingCommand command) {
        var listing = listingRepository.findByPartitionAndListingPath(partitionService.getPartition(), command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        return listingService.toDto(listing);
    }
}
