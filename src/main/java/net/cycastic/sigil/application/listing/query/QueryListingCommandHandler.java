package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.listing.service.ListingService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.repository.ProjectRepository;
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
    private final ProjectRepository projectRepository;

    private PageResponseDto<ListingDto> handle(QueryListingCommand command, @NotNull Integer projectId, boolean verifyAccess){
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var page = listingRepository.findListingsByProjectAndListingPathStartingWith(project,
                command.getPrefix(),
                command.toPageable());
        var currentUserId = loggedUserAccessor.tryGetUserId();
        if (loggedUserAccessor.isAdmin() ||
                (currentUserId.isPresent() &&
                        project.getUser().getId().equals(currentUserId.get()))){
            return listingService.toDto(page);
        }

        if (verifyAccess) {
            listingService.verifyAccess(project, page.getContent().stream().map(Listing::getListingPath));
        }
        return listingService.toDto(page);
    }

    @Override
    public PageResponseDto<ListingDto> handle(QueryListingCommand command) {
        if (command.getProjectId() != null){
            return handle(command, command.getProjectId(), true);
        }

        return handle(command, loggedUserAccessor.getProjectId(), false);
    }
}
