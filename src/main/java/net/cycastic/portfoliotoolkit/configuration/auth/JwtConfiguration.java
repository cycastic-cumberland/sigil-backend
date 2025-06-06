package net.cycastic.portfoliotoolkit.configuration.auth;

import jakarta.validation.constraints.Null;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.auth.jwt")
public class JwtConfiguration {
    private long validForMillis;
    private String publicKey;
    private String privateKey;
    @Null
    private String issuer;
}
