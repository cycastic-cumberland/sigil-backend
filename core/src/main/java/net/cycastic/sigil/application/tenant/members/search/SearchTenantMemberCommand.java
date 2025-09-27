package net.cycastic.sigil.application.tenant.members.search;

import an.awesome.pipelinr.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.tenant.TenantUserDto;
import org.springframework.data.domain.Sort;
import jakarta.annotation.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchTenantMemberCommand extends PageRequestDto implements Command<PageResponseDto<TenantUserDto>> {
    @Nullable
    private String contentTerm;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("email").ascending();
    }
}
