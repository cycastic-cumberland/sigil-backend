package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryListingCommandHandler implements Command.Handler<QueryListingCommand, PageResponseDto<ListingDto>> {
    private final ListingService listingService;
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;

    @Override
    public PageResponseDto<ListingDto> handle(QueryListingCommand command) {
        var page = listingRepository.findListingsByPartitionAndListingPathStartingWith(partitionService.getPartition(),
                command.getPrefix(),
                command.toPageable());

        return listingService.toDto(page);
    }
}
