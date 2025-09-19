package net.cycastic.sigil.service.impl.notification;

import com.pusher.rest.Pusher;
import net.cycastic.sigil.configuration.notification.PusherConfigurations;
import net.cycastic.sigil.domain.dto.notifications.PusherAuthenticationDataDto;
import net.cycastic.sigil.service.notification.NotificationSender;
import net.cycastic.sigil.service.serializer.JsonSerializer;

import java.util.Objects;

public class PusherNotificationSender implements NotificationSender {
    private final Pusher pusher;
    private final JsonSerializer jsonSerializer;

    public PusherNotificationSender(PusherConfigurations configurations, JsonSerializer jsonSerializer){
        this.jsonSerializer = jsonSerializer;
        pusher = new Pusher(configurations.getAppId(), configurations.getKey(), configurations.getSecret());
        pusher.setCluster(configurations.getCluster());
        pusher.setEncrypted(Objects.requireNonNullElse(configurations.getEncrypted(), true));
    }
    @Override
    public void sendNotification(String channel, String eventType, Object payload) {
        pusher.trigger(channel, eventType, payload);
    }

    public PusherAuthenticationDataDto authenticate(String socketId, String channelName){
        var data = pusher.authenticate(socketId, channelName);
        return jsonSerializer.deserialize(data, PusherAuthenticationDataDto.class);
    }
}
