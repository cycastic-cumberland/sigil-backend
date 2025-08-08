package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.dto.auth.WebAuthnCredentialDto;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;

@Data
@Builder
public class KdfDetailsDto {
    private String algorithm;
    private KeyDerivationFunction.Parameters parameters;
    private String salt;
    private CipherDto wrappedUserKey;
    private WebAuthnCredentialDto webAuthnCredential;
    private long signatureVerificationWindow;
}
