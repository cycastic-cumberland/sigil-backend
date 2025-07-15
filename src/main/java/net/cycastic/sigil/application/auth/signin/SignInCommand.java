package net.cycastic.sigil.application.auth.signin;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.CredentialDto;

@Data
public class SignInCommand implements Command<CredentialDto> {
    private String payload;
    private String algorithm;
    private String signature;
}
