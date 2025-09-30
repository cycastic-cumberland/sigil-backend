package net.cycastic.sigil.application.tenant.members.invite;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantUserInvitationBackgroundJobHandler implements BackgroundJobHandler<TenantUserInvitationBackgroundJob> {
    private static final Logger logger = LoggerFactory.getLogger(TenantUserInvitationBackgroundJobHandler.class);
    private final TenantService tenantService;

    @Override
    public void process(TenantUserInvitationBackgroundJob data) {
        tenantService.sendTenantInvitation(data.getTenantId(), data.getInviterId(), data.getEmail(), data.getPermissions());
        logger.debug("Invited user {} to tenant {}", data.getEmail(), data.getTenantId());
    }
}
