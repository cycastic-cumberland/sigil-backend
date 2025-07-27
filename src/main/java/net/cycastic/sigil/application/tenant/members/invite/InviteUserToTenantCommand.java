package net.cycastic.sigil.application.tenant.members.invite;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteUserToTenantCommand implements Command<Void> {
    @NotNull
    private String email;

    private int permissions;
}
