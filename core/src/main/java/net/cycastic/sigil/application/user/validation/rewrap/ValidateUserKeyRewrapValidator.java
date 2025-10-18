package net.cycastic.sigil.application.user.validation.rewrap;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.CryptographicUtilities;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ValidateUserKeyRewrapValidator implements CommandValidator {
    private final UserService userService;

    @Override
    public void validate(Command command) {
        var rewrap = (ValidateUserKeyRewrap)command;
        var user = userService.getUser();
        CryptographicUtilities.verifySignature(Base64.getDecoder().decode(rewrap.getCiphertext()),
                Base64.getDecoder().decode(rewrap.getSignature()),
                rewrap.getSignatureAlgorithm(),
                CryptographicUtilities.Keys.decodeRSAPublicKey(user.getPublicRsaKey()));
    }

    @Override
    public boolean matches(Class klass) {
        return ValidateUserKeyRewrap.class.isAssignableFrom(klass);
    }
}
