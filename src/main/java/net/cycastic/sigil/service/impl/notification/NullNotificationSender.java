package net.cycastic.sigil.service.impl.notification;

import net.cycastic.sigil.service.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullNotificationSender implements NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(NullNotificationSender.class);

    public NullNotificationSender(){
        logger.warn("Using NullNotificationSender");
    }

    @Override
    public <T> void sendNotification(String channel, String eventType, T payload) {
        logger.info("Notification sent: channel={}, eventType={}, payload={}", channel, eventType, payload);
    }
}
