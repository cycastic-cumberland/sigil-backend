package net.cycastic.sigil.application.partition.member.query;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryPartitionMemberCommand extends PageRequestDto implements Command<PageResponseDto<PartitionUserDto>> {
    @Nullable
    @Size(min = 1)
    private String contentTerm;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("lastName").ascending();
    }
}
