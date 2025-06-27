package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemDto;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemType;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.ListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuerySingleLevelCommandHandler implements Command.Handler<QuerySingleLevelCommand, PageResponseDto<FolderItemDto>> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final ProjectRepository projectRepository;
    private final ListingRepository listingRepository;

    @Override
    public PageResponseDto<FolderItemDto> handle(QuerySingleLevelCommand command) {
        var folder = command.getFolder();
        if (!folder.startsWith("/")){
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")){
            folder = folder + '/';
        }
        var project = projectRepository.findById(loggedUserAccessor.getProjectId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var page = listingRepository.findItems(project, folder, command.toPageable());

        return PageResponseDto.fromDomain(page,
                f -> new FolderItemDto(f.getName(),
                        f.getModifiedAt(),
                        f.getType() == null ? FolderItemType.FOLDER : ApplicationUtilities.fromListingType(f.getType()),
                        f.getAttachmentUploadCompleted()));
    }
}
