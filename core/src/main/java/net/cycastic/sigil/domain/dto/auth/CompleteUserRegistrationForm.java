package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.annotation.Nullable;
import net.cycastic.sigil.application.misc.annotation.Base64String;

@Data
public class CompleteUserRegistrationForm {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Base64String
    private String publicRsaKey;

    @Nullable
    private CompletePasswordBasedCipher passwordCredential;

    @Nullable
    private WebAuthnCredentialDto webAuthnCredential;
}
