package net.cycastic.sigil.application.tenant.save;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class SaveTenantCommandHandler implements Command.Handler<SaveTenantCommand, IdDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    private Integer updateTenant(SaveTenantCommand command, Tenant tenant){
        tenant.setName(command.getName());
        tenant.setUpdatedAt(OffsetDateTime.now());

        tenantRepository.save(tenant);
        return tenant.getId();
    }

    private Integer createTenant(SaveTenantCommand command){
        var user = userRepository.findByIdForUpdate(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var project = Tenant.builder()
                .name(command.getName())
                .owner(user)
                .createdAt(OffsetDateTime.now())
                .build();
        tenantRepository.save(project);
        return project.getId();
    }

    @Override
    @Transactional
    public IdDto handle(SaveTenantCommand command) {
        if (command.getId() == null){
            var userId = command.getUserId();
            if (userId == null){
                command.setUserId(loggedUserAccessor.getUserId());
            } else if (!loggedUserAccessor.isAdmin()){ // only admins can have multiple tenants
                throw RequestException.forbidden();
            }
            return new IdDto(createTenant(command));
        }

        var userId = loggedUserAccessor.getUserId();
        var project = tenantRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "Could not find tenant"));
        if (!loggedUserAccessor.isAdmin() &&
                !project.getOwner().getId().equals(userId)){
            throw RequestException.forbidden();
        }

        return new IdDto(updateTenant(command, project));
    }
}
