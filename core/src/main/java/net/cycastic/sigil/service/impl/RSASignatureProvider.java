package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.configuration.security.ExtendibleSignatureConfiguration;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.service.AsymmetricSignatureProvider;
import net.cycastic.sigil.service.AsymmetricSignatureVerifier;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSASignatureProvider implements AsymmetricSignatureProvider, AsymmetricSignatureVerifier {
    private static final String ALGORITHM = "SHA256withRSA/PSS";
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RSASignatureProvider(ExtendibleSignatureConfiguration extendibleSignatureConfiguration){
        publicKey = CryptographicUtilities.Keys.decodeRSAPublicKey(Base64.getDecoder().decode(extendibleSignatureConfiguration.getPublicKey()));
        privateKey = CryptographicUtilities.Keys.decodeRSAPrivateKey(Base64.getDecoder().decode(extendibleSignatureConfiguration.getPrivateKey()));
    }

    @Override
    public byte[] sign(byte[] data) {
        return CryptographicUtilities.sign(privateKey, data, ALGORITHM);
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public boolean verify(byte[] data, byte[] signature) {
        return CryptographicUtilities.verifySignature(data, signature, ALGORITHM, publicKey);
    }
}
