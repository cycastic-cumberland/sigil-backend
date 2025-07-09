package net.cycastic.sigil.command;

import lombok.SneakyThrows;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CredentialDto;
import net.cycastic.sigil.service.auth.KeyDerivationFunction;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "verify-password", mixinStandardHelpOptions = true, description = "Create a new user")
public class VerifyPassword implements Callable<Integer> {
    @CommandLine.Option(names = "--email", required = true)
    private String email;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    private final UserService userService;
    private final KeyDerivationFunction keyDerivationFunction;

    public VerifyPassword(UserService userService, KeyDerivationFunction keyDerivationFunction){
        this.userService = userService;
        this.keyDerivationFunction = keyDerivationFunction;
    }

    private byte[] deriveKey(CredentialDto credential, String password){
        var settings = keyDerivationFunction.decodeSettings(credential.getKdfSettings());
        return keyDerivationFunction.derive(password.getBytes(StandardCharsets.UTF_8), settings.getSalt(), settings.getParameters())
                .getHash();
    }

    @SneakyThrows
    private void verifyDecryptionKey(CredentialDto credential, String password){
        final var sampleText = "Hello World!";
        var wrapKey = new SecretKeySpec(deriveKey(credential, password), "AES");
        var unwrappedPrivateKey = CryptographicUtilities.decrypt(wrapKey,
                Base64.getDecoder().decode(credential.getWrappedUserKey().getIv()),
                Base64.getDecoder().decode(credential.getWrappedUserKey().getCipher()));
        var kf = KeyFactory.getInstance("RSA", "BC");
        var publicKey = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(credential.getPublicRsaKey())));
        var privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(unwrappedPrivateKey));
        var ciphertext = CryptographicUtilities.encrypt(publicKey, sampleText.getBytes(StandardCharsets.UTF_8)).getCipher();
        var plaintext = CryptographicUtilities.decrypt(privateKey, ciphertext);
        assert new String(plaintext, StandardCharsets.UTF_8).equals(sampleText);
    }

    @Override
    public Integer call() {
        var cred = userService.generateCredential(email, password);
        verifyDecryptionKey(cred, password);
        return 0;
    }
}
