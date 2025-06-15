package net.cycastic.portfoliotoolkit.application.email.smtp.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.dto.DecryptedSmtpCredentialDto;

@Data
public class GetSmtpCredentialCommand implements Command<DecryptedSmtpCredentialDto> {
    private int id;
}
