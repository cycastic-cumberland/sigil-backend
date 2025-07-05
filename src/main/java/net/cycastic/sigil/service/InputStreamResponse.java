package net.cycastic.sigil.service;

import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.Nullable;

public interface InputStreamResponse extends InputStreamSource {
    @Nullable Long getContentLength();
    @Nullable String getFileName();
    @Nullable String getMimeType();
}
