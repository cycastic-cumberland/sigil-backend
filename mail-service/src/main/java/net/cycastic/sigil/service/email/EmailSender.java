package net.cycastic.sigil.service.email;

import jakarta.annotation.Nullable;

import java.util.Map;

public interface EmailSender {
    <T extends EmailImage> void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody, @Nullable Map<String, T> imageStreamSource);
}
