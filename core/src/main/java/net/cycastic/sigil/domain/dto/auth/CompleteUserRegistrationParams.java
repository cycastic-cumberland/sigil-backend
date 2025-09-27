package net.cycastic.sigil.domain.dto.auth;

import lombok.Data;

@Data
public class CompleteUserRegistrationParams {
    private int userId;
    private String securityStamp;
    private long notValidBefore;
    private long notValidAfter;
}
