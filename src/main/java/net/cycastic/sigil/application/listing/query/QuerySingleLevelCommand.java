package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class QuerySingleLevelCommand extends PageRequestDto implements Command<PageResponseDto<FolderItemDto>> {
    private int partitionId;
    private @NotNull String folder;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("type").ascending()
                .and(Sort.by("name").ascending());
    }
}
