package net.cycastic.sigil.service;

public interface AsymmetricSignatureVerifier {
    boolean verify(byte[] data, byte[] signature);

    String getAlgorithm();
}
