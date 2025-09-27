package net.cycastic.sigil.service.notification;

public interface NotificationSender {
    void sendNotification(String channel, String eventType, Object payload);
}
