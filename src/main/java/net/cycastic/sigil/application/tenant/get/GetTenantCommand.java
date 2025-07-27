package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.tenant.TenantDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTenantCommand implements Command<TenantDto> {
    private int id;
}
