package net.cycastic.portfoliotoolkit.command;


import net.cycastic.portfoliotoolkit.service.auth.AsymmetricKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Base64;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "generate-keypair", mixinStandardHelpOptions = true, description = "Generate asymmetric key-pair")
public class GenerateKeyPair implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(GenerateKeyPair.class);

    private final AsymmetricKeyGenerator asymmetricKeyGenerator;

    public GenerateKeyPair(AsymmetricKeyGenerator asymmetricKeyGenerator) {
        this.asymmetricKeyGenerator = asymmetricKeyGenerator;
    }

    @Override
    public Integer call() {
        var keyPair = asymmetricKeyGenerator.generate();
        logger.info("Public key: {}", Base64.getEncoder().encodeToString(keyPair.publicKey().getEncoded()));
        logger.info("Private key: {}", Base64.getEncoder().encodeToString(keyPair.privateKey().getEncoded()));
        return 0;
    }
}
