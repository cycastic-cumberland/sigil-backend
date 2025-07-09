package net.cycastic.sigil.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteListingCommandHandler implements Command.Handler<DeleteListingCommand, @Null Object> {
    private final ListingService listingService;

    @Override
    @Transactional
    public @Null Object handle(DeleteListingCommand command) {
        listingService.deleteListingNoTransaction(command.getListingPath());
        return null;
    }
}
