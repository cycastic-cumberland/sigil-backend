package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.FolderItemType;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.repository.listing.ListingRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryListingSingleLevelCommandHandler implements Command.Handler<QueryListingSingleLevelCommand, PageResponseDto<FolderItemDto>> {
    private final ListingRepository listingRepository;
    private final PartitionService partitionService;

    @Override
    public PageResponseDto<FolderItemDto> handle(QueryListingSingleLevelCommand command) {
        var folder = command.getFolder();
        if (!folder.startsWith("/")){
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")){
            folder = folder + '/';
        }
        var page = listingRepository.findItems(partitionService.getPartition(), folder, command.toPageable());

        return PageResponseDto.fromDomain(page,
                f -> new FolderItemDto(f.getName(),
                        f.getModifiedAt(),
                        f.getType() == null ? FolderItemType.FOLDER : ApplicationUtilities.fromListingType(f.getType()),
                        f.getAttachmentUploadCompleted()));
    }
}
