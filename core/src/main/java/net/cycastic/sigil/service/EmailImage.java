package net.cycastic.sigil.service;

import org.springframework.core.io.InputStreamSource;

public interface EmailImage {
    String getFileName();
    String getMimeType();
    InputStreamSource getImageSource();
}
