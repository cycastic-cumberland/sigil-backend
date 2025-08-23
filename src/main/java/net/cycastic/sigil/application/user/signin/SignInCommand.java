package net.cycastic.sigil.application.user.signin;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

@Data
public class SignInCommand implements Command<CredentialDto> {
    @NotEmpty
    private String payload;

    @NotEmpty
    private String algorithm;

    @NotEmpty
    private String signature;
}
