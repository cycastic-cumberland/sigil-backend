package net.cycastic.sigil.application.tenant.members.search;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchTenantMemberEmailByPrefixCommand extends PageRequestDto implements Command<PageResponseDto<String>> {
    private String emailPrefix;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("email").ascending();
    }
}
