package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class CompleteUserRegistrationCommand implements Command<@Null Object> {
    private int userId;
    private String securityStamp;
    private long notValidBefore;
    private long notValidAfter;
}
