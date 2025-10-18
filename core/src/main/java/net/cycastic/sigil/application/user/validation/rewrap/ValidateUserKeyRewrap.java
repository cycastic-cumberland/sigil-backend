package net.cycastic.sigil.application.user.validation.rewrap;

public interface ValidateUserKeyRewrap {
    String getSignatureAlgorithm();

    String getCiphertext();

    String getSignature();
}
