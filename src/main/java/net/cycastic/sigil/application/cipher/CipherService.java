package net.cycastic.sigil.application.cipher;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.model.Cipher;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.EncryptionProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CipherService {
    private final EncryptionProvider encryptionProvider;
    private final DecryptionProvider decryptionProvider;

    public Cipher createServerManagedKey(byte[] key){
        var encrypted = encryptionProvider.encrypt(key);
        return new Cipher(CipherDecryptionMethod.SERVER_SIDE, null, encrypted);
    }

    public byte[] unwrapServerManagedKey(Cipher cipher){
        if (!cipher.getDecryptionMethod().equals(CipherDecryptionMethod.SERVER_SIDE)){
            throw new IllegalStateException("Not server managed cipher");
        }

        return decryptionProvider.decrypt(cipher.getCipher());
    }
}
