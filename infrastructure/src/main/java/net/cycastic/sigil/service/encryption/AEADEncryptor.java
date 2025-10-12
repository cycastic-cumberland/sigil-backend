package net.cycastic.sigil.service.encryption;

import javax.crypto.SecretKey;

public interface AEADEncryptor {
    boolean canDecrypt(String identifier);
    SecretKey createKey();
    SecretKey createKey(byte[] encoded);
    void encrypt(AEADEncryptionContext context);
    void decrypt(AEADDecryptionContext context);
}
