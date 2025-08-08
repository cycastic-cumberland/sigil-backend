package net.cycastic.sigil.domain.dto.keyring.interfaces;

public interface KdfDetails {
    byte[] getParameters();
    byte[] getSalt();
}
