package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryListingCommandHandler implements Command.Handler<QueryListingCommand, PageResponseDto<ListingDto>> {
    private final ListingService listingService;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final TenantRepository tenantRepository;

    private PageResponseDto<ListingDto> handle(QueryListingCommand command, @NotNull Integer projectId){
        var project = tenantRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var page = listingRepository.findListingsByTenantAndListingPathStartingWith(project,
                command.getPrefix(),
                command.toPageable());
        var currentUserId = loggedUserAccessor.tryGetUserId();
        if (loggedUserAccessor.isAdmin() ||
                (currentUserId.isPresent() &&
                        project.getOwner().getId().equals(currentUserId.get()))){
            return listingService.toDto(page);
        }

        return listingService.toDto(page);
    }

    @Override
    public PageResponseDto<ListingDto> handle(QueryListingCommand command) {
        return handle(command, loggedUserAccessor.getTenantId());
    }
}
