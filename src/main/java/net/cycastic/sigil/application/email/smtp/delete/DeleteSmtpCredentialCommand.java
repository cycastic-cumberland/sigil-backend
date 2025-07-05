package net.cycastic.sigil.application.email.smtp.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class DeleteSmtpCredentialCommand implements Command<@Null Object> {
    private int id;
}
