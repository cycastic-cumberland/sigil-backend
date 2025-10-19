package net.cycastic.sigil.configuration.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendibleSignatureConfiguration {
    private String publicKey;
    private String privateKey;
}
