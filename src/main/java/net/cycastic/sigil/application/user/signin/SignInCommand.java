package net.cycastic.sigil.application.user.signin;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

@Data
public class SignInCommand implements Command<CredentialDto> {
    private String payload;
    private String algorithm;
    private String signature;
}
