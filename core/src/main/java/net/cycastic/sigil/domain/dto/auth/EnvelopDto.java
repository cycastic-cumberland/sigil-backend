package net.cycastic.sigil.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import jakarta.annotation.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopDto {
    @Nullable
    private CipherDto passwordCipher;

    @Nullable
    private WebAuthnCredentialDto webAuthnCipher;
}
