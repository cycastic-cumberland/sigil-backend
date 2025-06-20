package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemDto;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemType;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemsDto;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class QuerySingleLevelCommandHandler implements Command.Handler<QuerySingleLevelCommand, FolderItemsDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ProjectRepository projectRepository;
    private final ListingRepository listingRepository;

    @Override
    public FolderItemsDto handle(QuerySingleLevelCommand command) {
        var folder = command.getFolder();
        if (!folder.startsWith("/")){
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")){
            folder = folder + '/';
        }
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var items = listingRepository.findItems(project, folder);
        return new FolderItemsDto(Stream.concat(items.stream()
                .filter(f -> f.getType() == null)
                .map(f -> new FolderItemDto(f.getListingPath(), FolderItemType.FOLDER)),
                items.stream()
                        .filter(f -> f.getType() != null)
                        .map(f -> new FolderItemDto(f.getListingPath(), ApplicationUtilities.fromListingType(f.getType()))))
                .toList());
    }
}
