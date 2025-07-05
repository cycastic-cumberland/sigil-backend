package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.FolderItemType;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuerySingleLevelCommandHandler implements Command.Handler<QuerySingleLevelCommand, PageResponseDto<FolderItemDto>> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
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
        var project = tenantRepository.findById(loggedUserAccessor.getTenantId())
                .orElseThrow(() -> new RequestException(404, "Project not found"));

        var page = listingRepository.findItems(project, folder, command.toPageable());

        return PageResponseDto.fromDomain(page,
                f -> new FolderItemDto(f.getName(),
                        f.getModifiedAt(),
                        f.getType() == null ? FolderItemType.FOLDER : ApplicationUtilities.fromListingType(f.getType()),
                        f.getAttachmentUploadCompleted()));
    }
}
