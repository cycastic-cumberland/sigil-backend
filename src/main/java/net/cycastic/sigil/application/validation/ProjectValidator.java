package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.repository.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectValidator implements CommandValidator{
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public void validate(Command command) {
        var projectIdOpt = loggedUserAccessor.tryGetTenantId();
        if (projectIdOpt.isEmpty()){
            return;
        }

        if (!loggedUserAccessor.isAdmin() && !tenantUserRepository.existsByTenant_IdAndUser_Id(projectIdOpt.get(), loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean matches(Command command) {
        return true;
    }
}
