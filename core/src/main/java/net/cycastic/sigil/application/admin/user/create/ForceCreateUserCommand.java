package net.cycastic.sigil.application.admin.user.create;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.model.tenant.UserStatus;

import java.util.List;

@Data
@RequireAdmin
@TransactionalCommand
public class ForceCreateUserCommand implements Command<IdDto> {
    @NotNull
    @Email
    private String email;

    @Nullable
    private List<String> roles;

    @NotNull
    private CompleteUserRegistrationForm form;

    private boolean emailVerified;

    @Nullable
    private UserStatus status;
}
