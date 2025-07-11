package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;

@Data
public class GetKdfSettingsCommand implements Command<KdfDetailsDto> {
    private String userEmail;
}
