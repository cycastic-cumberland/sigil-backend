package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class RegisterUserCommand implements Command<Void> {
    private String email;
}
