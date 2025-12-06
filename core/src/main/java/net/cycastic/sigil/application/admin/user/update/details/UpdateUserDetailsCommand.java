package net.cycastic.sigil.application.admin.user.update.details;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.Retry;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.model.tenant.UserStatus;

import java.util.List;

@Data
@RequireAdmin
@TransactionalCommand
@Retry(Retry.Event.STALE)
public class UpdateUserDetailsCommand implements Command<Void> {
    @Min(1)
    private int id;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @Email
    @NotEmpty
    private String email;

    @Nullable
    private List<String> roles;

    private boolean emailVerified;

    @NotNull
    private UserStatus status;
}
