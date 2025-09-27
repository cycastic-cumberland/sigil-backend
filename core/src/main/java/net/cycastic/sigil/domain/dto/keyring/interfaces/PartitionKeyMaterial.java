package net.cycastic.sigil.domain.dto.keyring.interfaces;

public interface PartitionKeyMaterial {
    int getPartitionId();
    long getCipherId();
    byte[] getIv();
    byte[] getCipher();
}
