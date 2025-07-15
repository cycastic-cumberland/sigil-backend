package net.cycastic.sigil.command;


import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.auth.AsymmetricKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "generate-keypair", mixinStandardHelpOptions = true, description = "Generate asymmetric key-pair")
public class GenerateKeyPair implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(GenerateKeyPair.class);

    @CommandLine.Option(names = "--algorithm", required = true)
    private String algorithm;

    private final List<AsymmetricKeyGenerator> asymmetricKeyGenerators;
    private final Optional<EncryptionProvider> encryptionProvider;

    public GenerateKeyPair(List<AsymmetricKeyGenerator> asymmetricKeyGenerators, Optional<EncryptionProvider> encryptionProvider) {
        this.asymmetricKeyGenerators = asymmetricKeyGenerators;
        this.encryptionProvider = encryptionProvider;
    }

    @Override
    public Integer call() {
        var asymmetricKeyGenerator = asymmetricKeyGenerators.stream()
                .filter(k -> k.algorithm().equals(algorithm))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find algorithm: " + algorithm));
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
