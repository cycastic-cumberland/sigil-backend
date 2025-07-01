package net.cycastic.portfoliotoolkit.service;

public interface EmailSender {
    void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody);
}
