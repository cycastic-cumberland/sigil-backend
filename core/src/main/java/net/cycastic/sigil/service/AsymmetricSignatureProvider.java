package net.cycastic.sigil.service;

public interface AsymmetricSignatureProvider {
    byte[] sign(byte[] data);

    String getAlgorithm();
}
