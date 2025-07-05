package net.cycastic.sigil.application.project.save;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.Tenant;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class SaveProjectCommandHandler implements Command.Handler<SaveProjectCommand, IdDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;

    private Integer updateProject(SaveProjectCommand command, Tenant tenant){
        tenant.setName(command.getName());
        tenant.setUpdatedAt(OffsetDateTime.now());

        tenantRepository.save(tenant);
        return tenant.getId();
    }

    @Override
    @Transactional
    public IdDto handle(SaveProjectCommand command) {
        if (command.getId() == null){
            throw new ForbiddenException();
        }

        var userId = loggedUserAccessor.getUserId();
        var project = tenantRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find project"));
        if (!loggedUserAccessor.isAdmin() &&
                !project.getOwner().getId().equals(userId)){
            throw new ForbiddenException();
        }

        return new IdDto(updateProject(command, project));
    }
}
