package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.AsymmetricDecryptionProvider;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EncryptionKeyHelper {
    private final LoggedUserAccessor loggedUserAccessor;
    private final AsymmetricDecryptionProvider asymmetricDecryptionProvider;

    public Optional<byte[]> tryGetEncryptionKey(){
        var key = loggedUserAccessor.tryGetEncryptionKey();
        if (key.isEmpty()){
            return Optional.empty();
        }
        var wrappedKey = key.get();
        var unwrappedKey = asymmetricDecryptionProvider.decrypt(wrappedKey);
        return Optional.of(Base64.getDecoder().decode(unwrappedKey));
    }

    public byte[] getPartitionKey(){
        return tryGetEncryptionKey()
                .orElseThrow(() -> RequestException.withExceptionCode("C400T009"));
    }
}
