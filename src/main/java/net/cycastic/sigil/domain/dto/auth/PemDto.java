package net.cycastic.sigil.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PemDto {
    private String publicKey;
    private int version;
}
