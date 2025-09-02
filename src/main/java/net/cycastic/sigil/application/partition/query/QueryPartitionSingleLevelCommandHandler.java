package net.cycastic.sigil.application.partition.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.FolderItemType;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.model.listing.Partition;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryPartitionSingleLevelCommandHandler implements Command.Handler<QueryPartitionSingleLevelCommand, PageResponseDto<FolderItemDto>> {
    private final PartitionRepository partitionRepository;
    private final TenantService tenantService;
    private final UserService userService;

    private static FolderItemType toItemType(PartitionRepository.FileItem fileItem, Map<String, PartitionType> partitionTypeMap){
        if (!fileItem.getIsPartition()){
            return FolderItemType.FOLDER;
        }

        var entry = partitionTypeMap.get(fileItem.getName());
        if (entry == null){
            throw new IllegalStateException("Query results are faulty: Could not get partition type");
        }

        return switch (entry){
            case GENERIC -> FolderItemType.PARTITION;
            case PROJECT -> FolderItemType.PROJECT_PARTITION;
        };
    }

    @Override
    public PageResponseDto<FolderItemDto> handle(QueryPartitionSingleLevelCommand command) {
        if (command.getOrderBy() != null){
            command.setOrderBy(command.getOrderBy().replaceAll("type:", "isPartition:"));
        }
        var folder = command.getFolder();
        if (!folder.startsWith("/")){
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")){
            folder = folder + '/';
        }

        var page = partitionRepository.findItems(tenantService.getTenant(), folder, userService.getUser(), command.toPageable());
        var partitionNames = page.get()
                .filter(PartitionRepository.FileItem::getIsPartition)
                .map(PartitionRepository.FileItem::getName)
                .toList();
        final var prefix = folder;
        final var partitionEntries = partitionRepository.findByPrefixAndNames(folder, partitionNames).stream()
                .collect(Collectors.toMap(
                        p -> {
                            var path = p.getPartitionPath();
                            return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
                        },
                        Partition::getPartitionType,
                        (a, b) -> a
                ));

        return PageResponseDto.fromDomain(page,
                f -> FolderItemDto.builder()
                        .name(f.getName())
                        .modifiedAt(f.getModifiedAt())
                        .type(toItemType(f, partitionEntries))
                        .build());
    }
}
