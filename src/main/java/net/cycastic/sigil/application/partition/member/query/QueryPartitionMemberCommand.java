package net.cycastic.sigil.application.partition.member.query;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.data.domain.Sort;

public class QueryPartitionMemberCommand extends PageRequestDto implements Command<PageResponseDto<PartitionUserDto>> {
    @Override
    protected Sort getDefaultSort() {
        return Sort.by("lastName").ascending();
    }
}
