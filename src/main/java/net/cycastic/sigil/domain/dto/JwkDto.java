package net.cycastic.sigil.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwkDto {
    @JsonProperty("kty")
    private String kty;
    @JsonProperty("crv")
    private String crv;
    @JsonProperty("use")
    private String use;
    @JsonProperty("y")
    private String y;
    @JsonProperty("x")
    private String x;
    @JsonProperty("kid")
    private String kid;
    @JsonProperty("alg")
    private String alg;
}
