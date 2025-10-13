package net.cycastic.sigil.domain.dto.auth;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.cycastic.sigil.service.impl.Argon2idPasswordHasher;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Argon2idKdfDetailsDto extends KdfDetailsDto{
    private Argon2idPasswordHasher.CipherConfigurations parameters;
}
