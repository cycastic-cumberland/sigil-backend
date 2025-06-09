package net.cycastic.portfoliotoolkit;

import net.cycastic.portfoliotoolkit.service.DecryptionProvider;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import net.cycastic.portfoliotoolkit.service.impl.HashicorpVaultEncryptionProvider;
import net.cycastic.portfoliotoolkit.service.impl.SymmetricEncryptionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EncryptionTests {
    private final HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider;
    private final SymmetricEncryptionProvider symmetricEncryptionProvider;

    @Autowired
    public EncryptionTests(HashicorpVaultEncryptionProvider hashicorpVaultEncryptionProvider, SymmetricEncryptionProvider symmetricEncryptionProvider) {
        this.hashicorpVaultEncryptionProvider = hashicorpVaultEncryptionProvider;
        this.symmetricEncryptionProvider = symmetricEncryptionProvider;
    }

    private static void testEncryptDecrypt(EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider){
        final var text = "Hello World!";

        var encrypted = encryptionProvider.encrypt(text);
        var decrypted = decryptionProvider.decrypt(encrypted);
        assert text.equals(decrypted);
    }

    @Test
    public void testHashicorpVaultTransitEncryption(){
        testEncryptDecrypt(hashicorpVaultEncryptionProvider, hashicorpVaultEncryptionProvider);
    }

    @Test
    public void testSymmetricEncryption(){
        testEncryptDecrypt(symmetricEncryptionProvider, symmetricEncryptionProvider);
    }
}
