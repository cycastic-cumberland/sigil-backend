package net.cycastic.sigil.application.partition.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryPartitionSingleLevelCommand extends PageRequestDto implements Command<PageResponseDto<FolderItemDto>> {
    private @NotNull String folder;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("isPartition").ascending()
                .and(Sort.by("name").ascending());
    }
}
