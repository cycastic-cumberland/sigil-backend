package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class CompleteUserRegistrationForm {
    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String publicRsaKey;

    @Nullable
    private CompletePasswordBasedCipher passwordCredential;

    @Nullable
    private WebAuthnCredentialDto webAuthnCredential;
}
