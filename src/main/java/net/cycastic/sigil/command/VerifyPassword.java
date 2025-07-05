package net.cycastic.sigil.command;

import lombok.SneakyThrows;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.domain.dto.CredentialDto;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.crypto.Cipher;
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

    public VerifyPassword(UserService userService){
        this.userService = userService;
    }

    @SneakyThrows
    private static void verifyDecryptionKey(CredentialDto credential, String password){
        final var sampleText = "Hello World!";
        var wrapKey = new SecretKeySpec(CryptographicUtilities.deriveKey(CryptographicUtilities.KEY_LENGTH,
                password.getBytes(StandardCharsets.UTF_8),
                null),
                "AES");
        var unwrappedPrivateKey = CryptographicUtilities.decrypt(wrapKey,
                Base64.getDecoder().decode(credential.getWrappedUserKey().getIv()),
                Base64.getDecoder().decode(credential.getWrappedUserKey().getCipher()));
        var kf = KeyFactory.getInstance("RSA", "BC");
        var pub = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(credential.getPublicRsaKey())));
        var priv = kf.generatePrivate(new PKCS8EncodedKeySpec(unwrappedPrivateKey));
        var enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        enc.init(Cipher.ENCRYPT_MODE, pub);
        var ciphertext = enc.doFinal(sampleText.getBytes(StandardCharsets.UTF_8));

        var dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        dec.init(Cipher.DECRYPT_MODE, priv);
        var plaintext = dec.doFinal(ciphertext);
        assert new String(plaintext, StandardCharsets.UTF_8).equals(sampleText);
    }

    @Override
    public Integer call() {
        var cred = userService.generateCredential(email, password);
        verifyDecryptionKey(cred, password);
        return 0;
    }
}
