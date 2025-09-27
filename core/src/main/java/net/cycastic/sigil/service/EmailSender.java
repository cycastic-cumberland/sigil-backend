package net.cycastic.sigil.service;

import jakarta.annotation.Nullable;

import java.util.Map;

public interface EmailSender {
    void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody, @Nullable Map<String, EmailImage> imageStreamSource);
}
