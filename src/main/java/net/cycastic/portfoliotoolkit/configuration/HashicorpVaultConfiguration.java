package net.cycastic.portfoliotoolkit.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.vault")
public class HashicorpVaultConfiguration {
    private String apiAddress;
    private String token;
    private String keyName;
    private int apiVersion = 1;
}
