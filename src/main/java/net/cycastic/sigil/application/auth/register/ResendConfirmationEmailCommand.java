package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import lombok.Data;

@Data
public class ResendConfirmationEmailCommand implements Command<Void> {
    private String email;
}
