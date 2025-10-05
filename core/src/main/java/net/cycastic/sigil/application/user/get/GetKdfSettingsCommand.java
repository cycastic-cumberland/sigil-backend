package net.cycastic.sigil.application.user.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.cycastic.sigil.domain.dto.auth.KdfDetailsDto;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;

@Data
public class GetKdfSettingsCommand implements Command<KdfDetailsDto> {
    @NotBlank
    private String userEmail;
    private AuthenticationMethod method;
}
