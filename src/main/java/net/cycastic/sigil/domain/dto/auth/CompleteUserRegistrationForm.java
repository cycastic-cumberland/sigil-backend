package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.annotation.Nullable;

@Data
public class CompleteUserRegistrationForm {
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String publicRsaKey;

    @Nullable
    private CompletePasswordBasedCipher passwordCredential;

    @Nullable
    private WebAuthnCredentialDto webAuthnCredential;
}
