package net.cycastic.sigil.application.tenant.members.remove;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.partition.validation.TenantPermission;
import net.cycastic.sigil.domain.ApplicationConstants;

@Data
@Retry(value = Retry.Event.STALE)
@TransactionalCommand
@TenantPermission(ApplicationConstants.TenantPermissions.MODERATE)
public class RemoveTenantMemberCommand implements Command<Void> {
    @Email
    @NotEmpty
    private String email;
}
