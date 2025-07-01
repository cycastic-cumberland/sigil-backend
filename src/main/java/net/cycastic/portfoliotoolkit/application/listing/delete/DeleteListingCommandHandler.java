package net.cycastic.portfoliotoolkit.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteListingCommandHandler implements Command.Handler<DeleteListingCommand, @Null Object> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingService listingService;

    @Override
    @Transactional
    public @Null Object handle(DeleteListingCommand command) {
        listingService.deleteListingNoTransaction(loggedUserAccessor.getProjectId(), command.getListingPath());
        return null;
    }
}
