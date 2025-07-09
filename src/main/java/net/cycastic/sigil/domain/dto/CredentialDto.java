package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDto {
    private int userId;
    private String userEmail;
    private String publicRsaKey;
    private String kdfSettings;
    private CipherDto wrappedUserKey;
    private String authToken;
}
