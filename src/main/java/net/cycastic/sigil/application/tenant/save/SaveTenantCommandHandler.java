package net.cycastic.sigil.application.tenant.save;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.Tenant;
import net.cycastic.sigil.domain.model.tenant.TenantUser;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.domain.repository.tenant.TenantRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SaveTenantCommandHandler implements Command.Handler<SaveTenantCommand, IdDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final UserRepository userRepository;

    private Integer updateTenant(SaveTenantCommand command, Tenant tenant){
        tenant.setName(command.getTenantName());
        tenant.setUpdatedAt(OffsetDateTime.now());
        if (command.getUsageType() != null){
            tenant.setUsageType(command.getUsageType());
        }

        tenantRepository.save(tenant);
        return tenant.getId();
    }

    private Integer createTenant(SaveTenantCommand command){
        var user = userRepository.findByIdForUpdate(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        // Admin can manually create multiple tenant on behalf of a user
        if (!loggedUserAccessor.isAdmin()){
            var ownershipCount = tenantRepository.countByOwner_Id(user.getId());
            if (ownershipCount > 0){
                throw new RequestException(400, "Tenant ownership exceeded");
            }
        }

        var tenant = Tenant.builder()
                .name(command.getTenantName())
                .owner(user)
                .usageType(Objects.requireNonNullElse(command.getUsageType(), UsageType.STANDARD))
                .createdAt(OffsetDateTime.now())
                .build();
        var tenantUser = TenantUser.builder()
                .tenant(tenant)
                .user(user)
                .build();
        user.setUpdatedAt(OffsetDateTime.now());
        tenantRepository.save(tenant);
        tenantUserRepository.save(tenantUser);
        userRepository.save(user);
        return tenant.getId();
    }

    @Override
    @Transactional
    public IdDto handle(SaveTenantCommand command) {
        if (command.getUsageType() != null && !loggedUserAccessor.isAdmin()){
            throw RequestException.forbidden();
        }
        if (command.getId() == null){
            var userId = command.getUserId();
            if (userId == null){
                command.setUserId(loggedUserAccessor.getUserId());
            } else if (!loggedUserAccessor.isAdmin()){
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
