package net.cycastic.sigil.domain.dto.keyring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.domain.dto.keyring.interfaces.WebAuthnBasedKdfDetails;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;

import java.util.Base64;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrfDerivedKeyMaterialDto extends KeyMaterialDto{
    private String credentialId;
    private String salt;
    private String[] transports;

    public static PrfDerivedKeyMaterialDto fromDomain(WebAuthnBasedKdfDetails details){
        return PrfDerivedKeyMaterialDto.builder()
                .id(details.getCipherId())
                .decryptionMethod(CipherDecryptionMethod.WEBAUTHN_KEY)
                .iv(details.getIv() != null ? Base64.getEncoder().encodeToString(details.getIv()) : null)
                .cipher(Base64.getEncoder().encodeToString(details.getCipher()))
                .credentialId(Base64.getEncoder().encodeToString(details.getWebAuthnCredentialId()))
                .salt(Base64.getEncoder().encodeToString(details.getWebAuthnSalt()))
                .transports(details.getWebAuthnTransports().split(","))
                .build();
    }
}
