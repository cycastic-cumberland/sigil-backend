package net.cycastic.sigil.application.email.smtp.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.DecryptedSmtpCredentialDto;

@Data
public class GetSmtpCredentialCommand implements Command<DecryptedSmtpCredentialDto> {
    private int id;
}
