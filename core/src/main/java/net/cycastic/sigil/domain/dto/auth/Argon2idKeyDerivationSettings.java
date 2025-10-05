package net.cycastic.sigil.domain.dto.auth;

import lombok.*;
import net.cycastic.sigil.domain.dto.keyring.interfaces.KdfDetails;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import net.cycastic.sigil.service.impl.Argon2idPasswordHasher;

import java.io.ByteArrayInputStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Argon2idKeyDerivationSettings implements KeyDerivationFunction.KeyDerivationSettings {
    private byte[] salt;
    private Argon2idPasswordHasher.CipherConfigurations parameters;

    @SneakyThrows
    public static Argon2idKeyDerivationSettings fromDetails(KdfDetails details){
        try (var stream = new ByteArrayInputStream(details.getParameters())){
            var argon2IdParams = Argon2idPasswordHasher.CipherConfigurations.decode(stream);
            return new Argon2idKeyDerivationSettings(details.getSalt(), argon2IdParams);
        }
    }
}
