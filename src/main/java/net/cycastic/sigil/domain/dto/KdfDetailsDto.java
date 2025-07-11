package net.cycastic.sigil.domain.dto;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;

@Data
@Builder
public class KdfDetailsDto {
    private String algorithm;
    private KeyDerivationFunction.Parameters parameters;
    private String salt;
}
