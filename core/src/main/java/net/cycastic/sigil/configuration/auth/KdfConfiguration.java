package net.cycastic.sigil.configuration.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.auth.kdf")
public class KdfConfiguration {
    private String maskingKey;
    private String maskingRsaPublicKey;
    private String maskingRsaPrivateKey;
}
