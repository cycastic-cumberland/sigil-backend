package net.cycastic.sigil.service.impl.notification;

import com.pusher.rest.Pusher;
import net.cycastic.sigil.configuration.notification.PusherConfigurations;
import net.cycastic.sigil.service.notification.NotificationSender;

import java.util.Objects;

public class PusherNotificationSender implements NotificationSender {
    private final Pusher pusher;

    public PusherNotificationSender(PusherConfigurations configurations){
        pusher = new Pusher(configurations.getAppId(), configurations.getKey(), configurations.getSecret());
        pusher.setCluster(configurations.getCluster());
        pusher.setEncrypted(Objects.requireNonNullElse(configurations.getEncrypted(), true));
    }
    @Override
    public void sendNotification(String channel, String eventType, Object payload) {
        pusher.trigger(channel, eventType, payload);
    }
}
