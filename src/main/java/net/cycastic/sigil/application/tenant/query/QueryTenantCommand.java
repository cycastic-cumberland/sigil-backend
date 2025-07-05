package net.cycastic.sigil.application.tenant.query;

import an.awesome.pipelinr.Command;
import lombok.*;
import net.cycastic.sigil.domain.dto.TenantDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryTenantCommand extends PageRequestDto implements Command<PageResponseDto<TenantDto>> {
    private Integer userId;
}
