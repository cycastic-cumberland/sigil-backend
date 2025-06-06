package net.cycastic.portfoliotoolkit.service.impl.auth;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.service.auth.AsymmetricKeyGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.KeyPairGenerator;

@Lazy
@Service
public class Ed25519AsymmetricKeyGenerator implements AsymmetricKeyGenerator {
    @Override
    @SneakyThrows
    public @NonNull KeyPair generate() {
        var kpg = KeyPairGenerator.getInstance("EC");
        var kp = kpg.generateKeyPair();
        return new KeyPair(kp.getPublic(), kp.getPrivate());
    }
}
