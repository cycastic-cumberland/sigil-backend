package net.cycastic.sigil.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Argon2idKdfDetailsDto extends KdfDetailsDto{
    private Argon2idKeyDerivationSettings parameters;
}
