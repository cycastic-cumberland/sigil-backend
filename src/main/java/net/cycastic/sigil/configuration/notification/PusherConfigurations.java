package net.cycastic.sigil.configuration.notification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.pusher")
public class PusherConfigurations {
    private String appId;
    private String key;
    private String secret;
    private String cluster;
    private Boolean encrypted;
}
