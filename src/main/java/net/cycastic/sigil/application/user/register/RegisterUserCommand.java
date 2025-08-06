package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterUserCommand implements Command<Void> {
    @NotNull
    @Email
    private String email;
}
