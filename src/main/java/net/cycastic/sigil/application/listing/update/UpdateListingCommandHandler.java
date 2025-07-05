package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class UpdateListingCommandHandler implements Command.Handler<UpdateListingCommand, @Null Object> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;

    @Override
    @Transactional
    public @Null Object handle(UpdateListingCommand command) {
        var listing = listingRepository.findByProject_IdAndListingPath(loggedUserAccessor.getProjectId(), command.getPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        listing.setListingPath(command.getNewPath());
        listing.setUpdatedAt(OffsetDateTime.now());

        listingRepository.save(listing);
        return null;
    }
}
