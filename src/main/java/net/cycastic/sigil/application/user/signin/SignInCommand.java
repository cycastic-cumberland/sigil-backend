package net.cycastic.sigil.application.user.signin;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.cycastic.sigil.domain.dto.auth.CredentialDto;

@Data
public class SignInCommand implements Command<CredentialDto> {
    @NotBlank
    private String payload;

    @NotBlank
    private String algorithm;

    @NotBlank
    private String signature;
}
