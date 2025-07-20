package net.cycastic.sigil.domain.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OidcConfigurationDto {
    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("jwks_uri")
    private String jwksUri;
}
