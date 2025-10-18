package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotEmpty
    private String credentialId;

    @NotEmpty
    private String salt;

    @NotNull
    private String[] transports;

    @NotNull
    private CipherDto wrappedUserKey;

    public static WebAuthnCredentialDto fromDomain(WebAuthnCredential credential){
        return WebAuthnCredentialDto.builder()
                .credentialId(Base64.getEncoder().encodeToString(credential.getCredentialId()))
                .salt(Base64.getEncoder().encodeToString(credential.getSalt()))
                .transports(credential.getTransports().split(","))
                .build();
    }
}
