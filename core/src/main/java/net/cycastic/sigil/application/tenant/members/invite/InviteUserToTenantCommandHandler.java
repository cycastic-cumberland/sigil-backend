package net.cycastic.sigil.application.tenant.members.invite;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.configuration.application.TenantConfigurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class InviteUserToTenantCommandHandler implements Command.Handler<InviteUserToTenantCommand, Void> {
    private static final Logger logger = LoggerFactory.getLogger(InviteUserToTenantCommandHandler.class);
    private final TenantService tenantService;
    private final TenantUserRepository tenantUserRepository;
    private final TenantConfigurations tenantConfigurations;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TaskExecutor taskScheduler;

    @Override
    public Void handle(final InviteUserToTenantCommand command) {
        tenantService.checkPermission(ApplicationConstants.TenantPermissions.MODERATE);
        final var tenantId = loggedUserAccessor.getTenantId();
        final var inviterId = loggedUserAccessor.getUserId();
        var tenantUserOpt = tenantUserRepository.findByTenantIdAndUserEmail(tenantId, command.getEmail());
        if (tenantUserOpt.isPresent()){
            var tenantUser = tenantUserOpt.get();
            if (tenantUser.getLastInvited() == null){
                throw new RequestException(400, "User is already a member");
            }
            var secondsElapsed = Duration.between(tenantUser.getLastInvited(), OffsetDateTime.now()).getSeconds();
            if (secondsElapsed < tenantConfigurations.getResendInvitationLimitSeconds()){
                throw RequestException.withExceptionCode("C400T004", tenantConfigurations.getResendInvitationLimitSeconds() - secondsElapsed);
            }
        }

        taskScheduler.execute(() -> {
            try {
                tenantService.sendTenantInvitation(tenantId, inviterId, command.getEmail(), command.getPermissions());
                logger.debug("Invited user {} to tenant {}", command.getEmail(), tenantId);
            } catch (Exception e){
                logger.debug("Failed to invite user {} to tenant {}", command.getEmail(), tenantId, e);
            }
        });
        return null;
    }
}
