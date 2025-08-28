package net.cycastic.sigil.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteListingCommandHandler implements Command.Handler<DeleteListingCommand, Void> {
    private final ListingService listingService;

    @Override
    public Void handle(DeleteListingCommand command) {
        listingService.deleteListingNoTransaction(command.getListingPath());
        return null;
    }
}
