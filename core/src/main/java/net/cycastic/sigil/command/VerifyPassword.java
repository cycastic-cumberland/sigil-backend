package net.cycastic.sigil.command;

import lombok.SneakyThrows;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "verify-password", mixinStandardHelpOptions = true, description = "Create a new user")
public class VerifyPassword implements Callable<Integer> {
    @CommandLine.Option(names = "--email", required = true)
    private String email;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    private final UserRepository userRepository;
    private final UserService userService;
    private final KeyDerivationFunction keyDerivationFunction;

    public VerifyPassword(UserRepository userRepository, UserService userService, KeyDerivationFunction keyDerivationFunction){
        this.userRepository = userRepository;
        this.userService = userService;
        this.keyDerivationFunction = keyDerivationFunction;
    }

    private byte[] deriveKey(KeyDerivationFunction.KeyDerivationSettings settings, String password){
        return keyDerivationFunction.derive(password.getBytes(StandardCharsets.UTF_8), settings.getSalt(), settings.getParameters())
                .getHash();
    }

    @Override
    @SneakyThrows
    public Integer call() {
        final var signingAlgorithm = "SHA256withRSA/PSS";
        var user = userRepository.getByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        var cipher = user.getWrappedUserKey();
        KeyDerivationFunction.Parameters parameters;
        try (var stream = new ByteArrayInputStream(user.getKdfSettings())){
            parameters = keyDerivationFunction.getParameters(stream);
        }
        var wrapKey = new SecretKeySpec(deriveKey(new KeyDerivationFunction.KeyDerivationSettings() {
            @Override
            public byte[] getSalt() {
                return user.getKdfSalt();
            }

            @Override
            public KeyDerivationFunction.Parameters getParameters() {
                return parameters;
            }
        }, password), "AES");
        var decryptedPrivateKey = CryptographicUtilities.decrypt(wrapKey, cipher.getIv(), cipher.getCipher());
        var privateKey = CryptographicUtilities.Keys.decodeRSAPrivateKey(decryptedPrivateKey);

        var timeStep = CryptographicUtilities.TOTP.getTimeStamp(Instant.now().getEpochSecond(), UserService.SIGNATURE_VERIFICATION_WINDOW);
        var payload = String.format("%s:%d", user.getNormalizedEmail(), timeStep);
        var signature = CryptographicUtilities.sign(privateKey, payload.getBytes(StandardCharsets.UTF_8), signingAlgorithm);
        userService.generateCredential(payload, signingAlgorithm, signature);
        return 0;
    }
}
