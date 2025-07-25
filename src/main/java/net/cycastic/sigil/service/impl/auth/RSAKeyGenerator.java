package net.cycastic.sigil.service.impl.auth;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.service.auth.AsymmetricKeyGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.KeyPairGenerator;

@Lazy
@Service
public class RSAKeyGenerator implements AsymmetricKeyGenerator {
    private static final int KEY_SIZE = 2048;

    // yes, yes, virtual calls... whatever...
    public static final RSAKeyGenerator INSTANCE = new RSAKeyGenerator();

    @Override
    public String algorithm() {
        return "RSA";
    }

    @Override
    @SneakyThrows
    public @NonNull KeyPair generate() {
        var keyGen = KeyPairGenerator.getInstance(algorithm(), "BC");
        keyGen.initialize(KEY_SIZE);
        var kp = keyGen.generateKeyPair();
        return new KeyPair(kp.getPublic(), kp.getPrivate());
    }
}
