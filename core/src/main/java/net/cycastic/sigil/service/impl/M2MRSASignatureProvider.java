package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.configuration.security.M2MSignatureConfiguration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service("m2MRSASignatureProvider")
public class M2MRSASignatureProvider extends RSASignatureProvider{

    public M2MRSASignatureProvider(M2MSignatureConfiguration m2MSignatureConfiguration) {
        super(m2MSignatureConfiguration);
    }
}
