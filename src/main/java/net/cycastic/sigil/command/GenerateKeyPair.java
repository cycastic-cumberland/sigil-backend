package net.cycastic.sigil.command;


import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.auth.AsymmetricKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "generate-keypair", mixinStandardHelpOptions = true, description = "Generate asymmetric key-pair")
public class GenerateKeyPair implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(GenerateKeyPair.class);

    private final AsymmetricKeyGenerator asymmetricKeyGenerator;
    private final Optional<EncryptionProvider> encryptionProvider;

    public GenerateKeyPair(AsymmetricKeyGenerator asymmetricKeyGenerator, Optional<EncryptionProvider> encryptionProvider) {
        this.asymmetricKeyGenerator = asymmetricKeyGenerator;
        this.encryptionProvider = encryptionProvider;
    }

    @Override
    public Integer call() {
        var keyPair = asymmetricKeyGenerator.generate();
        var privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivateKey().getEncoded());
        logger.info("Public key: {}", Base64.getEncoder().encodeToString(keyPair.getPublicKey().getEncoded()));
        logger.info("Private key: {}", privateKey);
        if (encryptionProvider.isPresent()){
            var wrapped = encryptionProvider.get().encrypt(privateKey);
            logger.info("Private key (wrapped): {}", wrapped);
        }
        return 0;
    }
}
