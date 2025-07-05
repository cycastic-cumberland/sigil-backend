package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SymmetricKeyHelper {
    private final LoggedUserAccessor loggedUserAccessor;

    public byte[] getDerivedKey(int desiredLength){
        return CryptographicUtilities.deriveKey(desiredLength, loggedUserAccessor.getSymmetricKey(), null);
    }
}
