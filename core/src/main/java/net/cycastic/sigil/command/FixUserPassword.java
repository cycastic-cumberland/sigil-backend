package net.cycastic.sigil.command;


import lombok.SneakyThrows;
import net.cycastic.sigil.application.user.UserService;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "fix-user-password", mixinStandardHelpOptions = true, description = "Fix user password cipher")
public class FixUserPassword implements Callable<Integer> {
    @CommandLine.Option(names = "--user-id", required = true)
    private int userId;

    @CommandLine.Option(names = "--private", required = true)
    private String privateKeyPath;

    @CommandLine.Option(names = "--password", required = true)
    private String password;

    private final UserService userService;

    public FixUserPassword(UserService userService) {
        this.userService = userService;
    }

    @SneakyThrows
    private static PrivateKey loadPrivateKey(String filename) {
        var keyBytes = Files.readAllBytes(Paths.get(filename));
        var content = new String(keyBytes).trim();

        byte[] key;
        try {
            key = Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e){
            var keyString = content
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            key = Base64.getDecoder().decode(keyString);
        }

        var spec = new PKCS8EncodedKeySpec(key);
        var kf = KeyFactory.getInstance("RSA", "BC");
        return kf.generatePrivate(spec);
    }

    @Override
    public Integer call() {
        var privateKey = loadPrivateKey(privateKeyPath);
        userService.updatePasswordCipher(userId, privateKey, password);

        return 0;
    }
}
