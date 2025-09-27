package net.cycastic.sigil;

import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;
import net.cycastic.sigil.service.impl.HashicorpVaultEncryptionProvider;
import net.cycastic.sigil.service.impl.SymmetricEncryptionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static void testEncryptDecrypt(String text, EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider){
        var encrypted = encryptionProvider.encrypt(text);
        var decrypted = decryptionProvider.decrypt(encrypted);
        assertEquals(text, decrypted);
    }

    private static void testEncryptDecrypt(EncryptionProvider encryptionProvider, DecryptionProvider decryptionProvider){
        testEncryptDecrypt("Hello World!", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("Xin chào thế giới!", encryptionProvider, decryptionProvider);
        testEncryptDecrypt("CXNljNhbX+PV88d5OvRSJfABfPtETSk8DHXGLjSPvuy031tXmHnrGpjEr4hXW31jHgzUvF+f1zNO9fO35BE7hdtkzDttuXAyHTeJw2tpFfcYPSapALIC3iL4jGZnjzPZasl4WGS3wQcc5/KPfgx7WTFMHkAxsMiNIjMB5RhCZ5FIjPv6agei7CH0KHWUjl+YbZwXwgoWhao+EmotrqvKMTZgeRP4QUT1aq3m5d4yr1iwLj8LgRhFCZQEZPmY3L6SdIxKBZtmNgZRe0Dsj3N7wtlmx5SH0RLtzoaBQWMBqfQnExOkhsHAWdEfvZFcQJifWtAEPfuTacE2Vhoz3daP+tsZC2Od3rAAKTpe8w1E7es+gDxM1tQe6N1ppZxy7A4CJV7j9qunvFY5PhBxgrP5jxaaKyLBDJ8nz7Yh1hEaW3pwG5p9ONv3xowoU/hM+2KIYRMU0Ji5WEq/6eWaxxXkbp1+tQTXS+jCbBc/K0EpO+9uY5sAbvrLSYwVsZdE1IwL07zUqJwRh1AWtM+MPt0iS9aSoGRh6d6IULUYd/8wTt2eV7YyPvJGll7pMiFxgCZAppSLcO2EWBP4Dt75ls2fDnlSaZR8nsqD4ARmU5OSxO+Q9okzej1S7m556575pGKeOi22wuvgXKzAbRusI8ydHFGlIskRQW79rJdOAm8IgiCYvxQdILfKVmc5I1rt6TAEDdPvDh4t3LYKcHtrxjh4/Q3wypiwqjQrve5nF78P3gVDjkyxeIPRo2dd/9fVi1yYpkCjRycg+sLlbHdPRviIZc/+MIRQ4TtZl4v4KC+4qFOJBS5p+G6TPEkZa+AHjXHtQuS+h4WaYbvHEE/M3uMm5Kz1/BeBUIGK/resqfeFzo//EDGz6myTTiDeawGxAG6fgbw1D2EKoBO39vTZJYSQCPfQ51PCqjUHC9p79k5jH6NiuvX/lSHImycxaEmyjuqKStDJBei2V3WnVgVxjf5N79UITfFmMEDjbI9IEQHGqn5kerLUqZ+HYHFHyI5O9rVr+8kO65MTHWo6SzHLoq1lxEJ+SdT6oUW4NyXnyTUTTJBQCouko0JG7rbXO5N/dzzYgnC0+2J67cRm5zKa59J7+rEqseWrU2Ek11VYB2+cZQOe0tO7AhPKF+soEt94pc0bpjg2zswGrbn5z3JsovKRxMeZUBZaBOaI/tYSaMd+9uXg6Oti4QBd8t819WbBhDRX9k81tne0yFvaNGgCAnb2jWt4tLd9yUtEeMYBoiYpdArL3sIRSOaiOaWGjM7+V6fgRDmjxXGeKte6selB9E6VGTVQBsY6go0xy1eVAfhEz813/eb+eu3PyaBn8eLaWMqorXdSji8O85ljY+DdxLwYkg==", encryptionProvider, decryptionProvider);
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
