package net.cycastic.sigil.application.listing.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetListingCommandHandler implements Command.Handler<GetListingCommand, ListingDto> {
    private final ListingService listingService;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final TenantRepository tenantRepository;

    private ListingDto handle(GetListingCommand command, @NotNull Integer projectId){
        var project = tenantRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var listing = listingRepository.findByTenantAndListingPath(project, command.getListingPath())
                .orElseThrow(() -> new RequestException(404, "Listing not found"));
        return listingService.toDto(listing);
    }

    @Override
    public ListingDto handle(GetListingCommand command) {
        return handle(command, loggedUserAccessor.getTenantId());
    }
}
