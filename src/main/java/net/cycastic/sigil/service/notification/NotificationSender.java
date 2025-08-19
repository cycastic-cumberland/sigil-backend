package net.cycastic.sigil.service.notification;

public interface NotificationSender {
    <T> void sendNotification(String channel, String eventType, T payload);
}
