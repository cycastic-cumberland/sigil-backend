package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.configuration.auth.KdfConfiguration;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class GetKdfSettingsCommandHandler implements Command.Handler<GetKdfSettingsCommand, KdfDetailsDto> {
    private final UserRepository userRepository;
    private final KeyDerivationFunction keyDerivationFunction;
    private final KeyDerivationFunction.Parameters defaultParameters;
    private final byte[] maskingSalt;

    @Autowired
    public GetKdfSettingsCommandHandler(UserRepository userRepository,
                                        KeyDerivationFunction keyDerivationFunction,
                                        KdfConfiguration kdfConfiguration) {
        this.userRepository = userRepository;
        this.keyDerivationFunction = keyDerivationFunction;

        defaultParameters = keyDerivationFunction.getDefaultParameters();
        byte[] salt;
        if (kdfConfiguration.getMaskingSalt() != null){
            try {
                salt = Base64.getDecoder().decode(kdfConfiguration.getMaskingSalt());
                if (salt.length != KeyDerivationFunction.SALT_SIZE) {
                    salt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, kdfConfiguration.getMaskingSalt().getBytes(StandardCharsets.UTF_8), null);
                }
            } catch (IllegalArgumentException e){
                salt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, kdfConfiguration.getMaskingSalt().getBytes(StandardCharsets.UTF_8), null);
            }
        } else {
            salt = new byte[KeyDerivationFunction.SALT_SIZE];
        }

        maskingSalt = salt;
    }

    @Override
    public KdfDetailsDto handle(GetKdfSettingsCommand command) {
        final var dummySalt = CryptographicUtilities.deriveKey(KeyDerivationFunction.SALT_SIZE, command.getUserEmail().getBytes(StandardCharsets.UTF_8), maskingSalt);
        var user = userRepository.getByEmail(command.getUserEmail());
        KeyDerivationFunction.KeyDerivationSettings settings;
        if (user.isPresent()){
            var encodedPassword = user.get().getHashedPassword();
            settings = keyDerivationFunction.decodeSettings(encodedPassword);
        } else {
            settings = new KeyDerivationFunction.KeyDerivationSettings() {
                @Override
                public byte[] getSalt() {
                    return dummySalt;
                }

                @Override
                public KeyDerivationFunction.Parameters getParameters() {
                    return defaultParameters;
                }
            };
        }

        return KdfDetailsDto.builder()
                .algorithm(keyDerivationFunction.getIdentifier())
                .parameters(settings.getParameters())
                .salt(Base64.getEncoder().encodeToString(settings.getSalt()))
                .build();
    }
}
