package net.cycastic.sigil.application.tenant.members.invite;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;
import net.cycastic.sigil.application.presigned.PresignedRequest;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.dto.tenant.TenantInvitationParams;

@Data
@Builder
@TransactionalCommand
public class CompleteTenantInvitationCommand implements PresignedRequest, Command<Void> {
    private TenantInvitationParams queryParams;
    private CompleteUserRegistrationForm form;

    @Override
    public long getNotValidBefore() {
        return queryParams.getNotValidBefore();
    }

    @Override
    public long getNotValidAfter() {
        return queryParams.getNotValidAfter();
    }
}
