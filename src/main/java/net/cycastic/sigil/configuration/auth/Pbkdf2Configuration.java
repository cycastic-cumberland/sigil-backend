package net.cycastic.sigil.configuration.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.auth.pbkdf2")
public class Pbkdf2Configuration {
    private int iterations = 310_000;
}
