package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.application.presigned.PresignedRequest;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationForm;
import net.cycastic.sigil.domain.dto.auth.CompleteUserRegistrationParams;

@Data
@Builder
public class CompleteUserRegistrationCommand implements PresignedRequest, Command<Void> {
    private int userId;
    private String securityStamp;
    private long notValidBefore;
    private long notValidAfter;

    private CompleteUserRegistrationForm form;

    public static CompleteUserRegistrationCommand fromDomain(CompleteUserRegistrationParams queryParams,
                                                             CompleteUserRegistrationForm form){
        return CompleteUserRegistrationCommand.builder()
                .userId(queryParams.getUserId())
                .securityStamp(queryParams.getSecurityStamp())
                .notValidBefore(queryParams.getNotValidBefore())
                .notValidAfter(queryParams.getNotValidAfter())
                .form(form)
                .build();
    }
}
