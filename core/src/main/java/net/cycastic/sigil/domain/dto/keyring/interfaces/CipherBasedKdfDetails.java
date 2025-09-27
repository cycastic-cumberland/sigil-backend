package net.cycastic.sigil.domain.dto.keyring.interfaces;

public interface CipherBasedKdfDetails extends KdfDetails{
    long getCipherId();
    byte[] getIv();
    byte[] getCipher();
}
