package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class RegisterUserCommand implements Command<@Null Object> {
    private String email;

    private String firstName;

    private String lastName;

    private String password;
}
