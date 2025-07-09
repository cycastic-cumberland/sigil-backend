package net.cycastic.sigil.application.partition.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.FolderItemType;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryPartitionSingleLevelCommandHandler implements Command.Handler<QueryPartitionSingleLevelCommand, PageResponseDto<FolderItemDto>> {
    private final PartitionRepository partitionRepository;
    private final TenantService tenantService;

    @Override
    public PageResponseDto<FolderItemDto> handle(QueryPartitionSingleLevelCommand command) {
        var folder = command.getFolder();
        if (!folder.startsWith("/")){
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")){
            folder = folder + '/';
        }

        var page = partitionRepository.findItems(tenantService.getTenant(), folder, command.toPageable());
        return PageResponseDto.fromDomain(page,
                f -> new FolderItemDto(f.getName(),
                        f.getModifiedAt(),
                        f.getIsFolder() ? FolderItemType.FOLDER : FolderItemType.PARTITION,
                        false));
    }
}
