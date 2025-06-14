package net.cycastic.portfoliotoolkit.configuration.auth;

import lombok.*;
import net.cycastic.portfoliotoolkit.configuration.BaseJwtConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "application.auth.jwt")
public class JwtConfiguration extends BaseJwtConfiguration {
    private String publicKey;
    private String privateKey;
}
