package net.cycastic.sigil.domain.dto.auth;

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
    private String authToken;
}
