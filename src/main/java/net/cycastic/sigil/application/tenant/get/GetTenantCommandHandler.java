package net.cycastic.sigil.application.tenant.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.dto.ProjectDto;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTenantCommandHandler implements Command.Handler<GetTenantCommand, ProjectDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;

    @Override
    public ProjectDto handle(GetTenantCommand command) {
        var userId = loggedUserAccessor.getUserId();
        var project = tenantRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find project"));
        if (!loggedUserAccessor.isAdmin() &&
            !project.getOwner().getId().equals(userId)){
            throw new ForbiddenException();
        }
        return ProjectDto.fromDomain(project);
    }
}
