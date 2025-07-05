package net.cycastic.sigil.application.tenant.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DeleteTenantCommandHandler implements Command.Handler<DeleteTenantCommand, @Null Object> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;

    @Override
    public @Null Object handle(DeleteTenantCommand command) {
        var userId = loggedUserAccessor.getUserId();
        var project = tenantRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find tenant"));
        if (!loggedUserAccessor.isAdmin() &&
                !project.getOwner().getId().equals(userId)){
            throw new ForbiddenException();
        }

        project.setRemovedAt(OffsetDateTime.now());
        tenantRepository.save(project);
        return null;
    }
}
