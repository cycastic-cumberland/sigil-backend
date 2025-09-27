package net.cycastic.sigil.domain.dto.keyring.interfaces;

public interface WebAuthnBasedKdfDetails extends CipherBasedKdfDetails {
    byte[] getWebAuthnCredentialId();
    byte[] getWebAuthnSalt();
    String getWebAuthnTransports();
}
