package net.cycastic.sigil.configuration.notification;

import net.cycastic.sigil.service.impl.notification.NullNotificationSender;
import net.cycastic.sigil.service.impl.notification.PusherNotificationSender;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationBean {
    @Bean
    public NotificationSender notificationSender(PusherConfigurations pusherConfigurations){
        if (pusherConfigurations.getAppId() == null){
            return new NullNotificationSender();
        }

        return new PusherNotificationSender(pusherConfigurations);
    }
}
