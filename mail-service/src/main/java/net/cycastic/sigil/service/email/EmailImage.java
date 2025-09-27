package net.cycastic.sigil.service.email;

import org.springframework.core.io.InputStreamSource;

public interface EmailImage {
    String getFileName();
    String getMimeType();
    InputStreamSource getImageSource();
}
