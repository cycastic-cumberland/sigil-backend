package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;
import net.cycastic.sigil.domain.dto.auth.AuthenticationMethod;

@Data
public class GetKdfSettingsCommand implements Command<KdfDetailsDto> {
    @NotEmpty
    private String userEmail;
    private AuthenticationMethod method;
}
