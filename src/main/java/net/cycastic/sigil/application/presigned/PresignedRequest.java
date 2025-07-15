package net.cycastic.sigil.application.presigned;

public interface PresignedRequest {
    long getNotValidBefore();
    long getNotValidAfter();
}
