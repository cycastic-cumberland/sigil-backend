package net.cycastic.portfoliotoolkit;

import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.service.DecryptionProvider;
import net.cycastic.portfoliotoolkit.service.EncryptionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EncryptionTests {
    private final EncryptionProvider encryptionProvider;
    private final DecryptionProvider decryptionProvider;

    @Autowired
    public EncryptionTests(EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider) {
        this.encryptionProvider = encryptionProvider;
        this.decryptionProvider = decryptionProvider;
    }

    @Test
    public void testEncryption(){
        final var text = "Hello World!";

        var encrypted = encryptionProvider.encrypt(text);
        var decrypted = decryptionProvider.decrypt(encrypted);
        assert text.equals(decrypted);
    }
}
