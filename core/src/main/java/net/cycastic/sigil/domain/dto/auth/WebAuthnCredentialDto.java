package net.cycastic.sigil.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.WebAuthnCredential;

import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnCredentialDto {
    private String credentialId;
    private String salt;
    private String[] transports;
    private CipherDto wrappedUserKey;

    public static WebAuthnCredentialDto fromDomain(WebAuthnCredential credential){
        return WebAuthnCredentialDto.builder()
                .credentialId(Base64.getEncoder().encodeToString(credential.getCredentialId()))
                .salt(Base64.getEncoder().encodeToString(credential.getSalt()))
                .transports(credential.getTransports().split(","))
                .build();
    }
}
