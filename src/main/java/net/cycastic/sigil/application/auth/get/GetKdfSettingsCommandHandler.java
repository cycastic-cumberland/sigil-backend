package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.KdfDetailsDto;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GetKdfSettingsCommandHandler implements Command.Handler<GetKdfSettingsCommand, KdfDetailsDto> {
    private final UserRepository userRepository;
    private final KeyDerivationFunction keyDerivationFunction;

    @Override
    public KdfDetailsDto handle(GetKdfSettingsCommand command) {
        var randomBytes = new byte[32];
        CryptographicUtilities.generateRandom(randomBytes);
        var encodedPassword = keyDerivationFunction.encode(randomBytes);
        var user = userRepository.getByEmail(command.getUserEmail());
        if (user.isPresent()){
            encodedPassword = user.get().getHashedPassword();
        }

        var settings = keyDerivationFunction.decodeSettings(encodedPassword);
        return KdfDetailsDto.builder()
                .algorithm(keyDerivationFunction.getIdentifier())
                .parameters(settings.getParameters())
                .salt(Base64.getEncoder().encodeToString(settings.getSalt()))
                .build();
    }
}
