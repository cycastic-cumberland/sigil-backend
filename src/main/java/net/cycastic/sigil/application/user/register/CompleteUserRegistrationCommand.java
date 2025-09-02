package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.presigned.PresignedRequest;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationParams;

@Data
@TransactionalCommand
@Builder
public class CompleteUserRegistrationCommand implements PresignedRequest, Command<Void> {
    private CompleteUserRegistrationParams queryParams;
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
