package net.cycastic.sigil;

import net.cycastic.sigil.configuration.security.ExtendibleSignatureConfiguration;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.service.AsymmetricSignatureProvider;
import net.cycastic.sigil.service.AsymmetricSignatureVerifier;
import net.cycastic.sigil.service.impl.RSASignatureProvider;
import net.cycastic.sigil.service.impl.auth.RSAKeyGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.Security;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class SigningTest {
    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (SecurityException ignored){

        }
    }

    private final AsymmetricSignatureProvider asymmetricSignatureProvider;
    private final AsymmetricSignatureVerifier asymmetricSignatureVerifier;

    public SigningTest() {
        var keyPair = RSAKeyGenerator.INSTANCE.generate();
        var provider = new RSASignatureProvider(ExtendibleSignatureConfiguration.builder()
                .privateKey(Base64.getEncoder().encodeToString(keyPair.getPrivateKey().getEncoded()))
                .publicKey(Base64.getEncoder().encodeToString(keyPair.getPublicKey().getEncoded()))
                .build());
        this.asymmetricSignatureProvider = provider;
        this.asymmetricSignatureVerifier = provider;
    }

    @Test
    public void testSigningAndVerification(){
        var data = new byte[32];
        SlimCryptographicUtilities.generateRandom(data);
        var signature = asymmetricSignatureProvider.sign(data);
        assertTrue(asymmetricSignatureVerifier.verify(data, signature), "Signature verification failed");
    }
}
