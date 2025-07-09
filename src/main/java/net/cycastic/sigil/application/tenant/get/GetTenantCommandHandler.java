package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.dto.TenantDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTenantCommandHandler implements Command.Handler<GetTenantCommand, TenantDto> {
    private final TenantService tenantService;

    @Override
    public TenantDto handle(GetTenantCommand command) {
        return TenantDto.fromDomain(tenantService.getTenant());
    }
}
