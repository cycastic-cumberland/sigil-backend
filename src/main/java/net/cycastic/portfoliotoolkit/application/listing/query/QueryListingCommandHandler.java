package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.dto.paging.PageResponseDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryListingCommandHandler implements Command.Handler<QueryListingCommand, PageResponseDto<ListingDto>> {
    private static final byte[] HIGHEST_SEARCH_KEY = NsoUtilities.getHighestSearchKey();
    private static final byte[] EMPTY_SEARCH_KEY = new byte[0];

    private final ListingService listingService;
    private final LoggedUserAccessor loggedUserAccessor;
    private final ListingRepository listingRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PageResponseDto<ListingDto> handle(QueryListingCommand command) {
        var projectId = loggedUserAccessor.getProjectId();
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RequestException(404, "Project not found"));
        var page = listingRepository.findListingsByProjectAndSearchKeyGreaterThanEqualAndSearchKeyLessThan(project,
                ListingService.encodeSearchKey(command.getSearchPathLow()),
                ListingService.encodeSearchKey(command.getSearchPathHigh()),
                command.toPageable());
        var currentUserId = loggedUserAccessor.tryGetUserId();
        if (loggedUserAccessor.isAdmin() ||
                (currentUserId.isPresent() &&
                    project.getUser().getId().equals(currentUserId.get()))){
            return listingService.toDto(page);
        }

        var highestSearchKey = EMPTY_SEARCH_KEY;
        var lowestSearchKey = HIGHEST_SEARCH_KEY;
        for (var listing : page.getContent()){
            if (NsoUtilities.compareByteArrays(listing.getSearchKey(), highestSearchKey) > 0){
                highestSearchKey = listing.getSearchKey();
            }
            if (NsoUtilities.compareByteArrays(listing.getSearchKey(), lowestSearchKey) < 0){
                lowestSearchKey = listing.getSearchKey();
            }
        }

        listingService.verifyAccess(project, lowestSearchKey, highestSearchKey);
        return listingService.toDto(page);
    }
}
