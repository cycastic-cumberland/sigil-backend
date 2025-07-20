package net.cycastic.sigil.command;

import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.domain.model.tenant.UsageType;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.service.PasswordValidator;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import net.cycastic.sigil.service.impl.auth.RSAKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "create-user", mixinStandardHelpOptions = true, description = "Create a new user")
public class CreateUser implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CreateUser.class);
    @CommandLine.Option(names = "--email", required = true)
    private String email;

    @CommandLine.Option(names = "--first-name", required = true)
    private String firstName;

    @CommandLine.Option(names = "--last-name", required = true)
    private String lastName;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    @CommandLine.Option(names = "--usage-type")
    private UsageType usageType;

    @CommandLine.Option(names = "--roles", required = true, split = ",")
    private List<String> roles;

    private final UserService userService;
    private final PasswordValidator passwordValidator;
    private final KeyDerivationFunction keyDerivationFunction;

    public CreateUser(UserService userService, PasswordValidator passwordValidator, KeyDerivationFunction keyDerivationFunction){
        this.userService = userService;
        this.passwordValidator = passwordValidator;
        this.keyDerivationFunction = keyDerivationFunction;
    }

    @Override
    public Integer call() {
        passwordValidator.validate(password);
        var kdfSalt = new byte[KeyDerivationFunction.SALT_SIZE];
        CryptographicUtilities.generateRandom(kdfSalt);
        var kekKdf = keyDerivationFunction.derive(password.getBytes(StandardCharsets.UTF_8), kdfSalt, keyDerivationFunction.getDefaultParameters());
        var keyPair = RSAKeyGenerator.INSTANCE.generate();
        var keyEncryptionKey = kekKdf.getHash();
        if (keyEncryptionKey.length != CryptographicUtilities.KEY_LENGTH){
            throw new RequestException(400, "Invalid key encryption key");
        }
        var wrapKey = new SecretKeySpec(keyEncryptionKey, "AES");
        var encodedPrivateKey = keyPair.getPrivateKey().getEncoded();
        var wrappedPrivateKey = CryptographicUtilities.encrypt(wrapKey, encodedPrivateKey);

        var encoder = Base64.getEncoder();
        var privateRsaKey = CipherDto.builder()
                .iv(encoder.encodeToString(wrappedPrivateKey.getIv()))
                .cipher(encoder.encodeToString(wrappedPrivateKey.getCipher()))
                .decryptionMethod(CipherDecryptionMethod.USER_PASSWORD)
                .build();
        userService.registerUser(email,
                firstName,
                lastName,
                encoder.encodeToString(keyPair.getPublicKey().getEncoded()),
                encoder.encodeToString(kdfSalt),
                encoder.encodeToString(kekKdf.getParameters().encode()),
                privateRsaKey,
                roles,
                UserStatus.ACTIVE,
                Objects.requireNonNullElse(usageType, UsageType.STANDARD),
                true);
        logger.info("User created.");
        return 0;
    }
}