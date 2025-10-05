package net.cycastic.sigil.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KdfDetailsDto {
    private String algorithm;
    private String salt;
    private CipherDto wrappedUserKey;
    private WebAuthnCredentialDto webAuthnCredential;
    private long signatureVerificationWindow;
}
