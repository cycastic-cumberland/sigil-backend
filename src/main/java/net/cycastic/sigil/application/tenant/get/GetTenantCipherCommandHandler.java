package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class GetTenantCipherCommandHandler implements Command.Handler<GetTenantCipherCommand, CipherDto> {
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    @Override
    public CipherDto handle(GetTenantCipherCommand command) {
        if (!loggedUserAccessor.isAdmin() && !tenantUserRepository.existsByTenant_IdAndUser_Id(command.getId(), loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        Supplier<RequestException> supplier = () -> new RequestException(404, "Cipher not found");
        var cipher = tenantUserRepository.findByTenant_IdAndUser_Id(command.getId(), loggedUserAccessor.getUserId())
                .orElseThrow(supplier)
                .getWrappedTenantKey();
        if (cipher == null){
            throw supplier.get();
        }

        return CipherDto.fromDomain(cipher);
    }
}
