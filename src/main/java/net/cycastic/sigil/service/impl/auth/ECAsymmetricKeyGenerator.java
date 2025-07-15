package net.cycastic.sigil.service.impl.auth;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.sigil.service.auth.AsymmetricKeyGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.KeyPairGenerator;

@Lazy
@Service
public class ECAsymmetricKeyGenerator implements AsymmetricKeyGenerator {
    @Override
    public String algorithm() {
        return "EC";
    }

    @Override
    @SneakyThrows
    public @NonNull KeyPair generate() {
        var kpg = KeyPairGenerator.getInstance(algorithm());
        var kp = kpg.generateKeyPair();
        return new KeyPair(kp.getPublic(), kp.getPrivate());
    }
}
