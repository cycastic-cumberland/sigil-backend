package net.cycastic.sigil.domain.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.misc.annotation.Base64String;
import net.cycastic.sigil.domain.dto.keyring.CipherDto;
import net.cycastic.sigil.domain.model.WebAuthnCredential;

import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnCredentialDto {
    @NotEmpty
    @Base64String
    private String credentialId;

    @NotEmpty
    @Base64String
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
                .wrappedUserKey(CipherDto.fromDomain(credential.getWrappedUserKey()))
                .build();
    }
}
