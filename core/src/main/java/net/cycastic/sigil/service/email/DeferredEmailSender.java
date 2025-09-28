package net.cycastic.sigil.service.email;

import jakarta.annotation.Nullable;

import java.util.Map;

public interface DeferredEmailSender extends EmailSender {
    void sendHtml(String to, String cc, String subject, String htmlBody, @Nullable Map<String, EmailImage> imageStreamSource);
}
