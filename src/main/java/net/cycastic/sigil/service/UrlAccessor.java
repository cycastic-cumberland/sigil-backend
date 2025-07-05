package net.cycastic.sigil.service;

public interface UrlAccessor {
    String getBackendOrigin();

    String getFrontendOrigin();
}
