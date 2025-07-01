package net.cycastic.portfoliotoolkit.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.CredentialDto;

@Data
public class SignInCommand implements Command<CredentialDto> {
    private String email;
    private String password;
}
