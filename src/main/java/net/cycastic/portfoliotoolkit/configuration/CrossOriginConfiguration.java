package net.cycastic.portfoliotoolkit.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.cors")
public class CrossOriginConfiguration {
    private String[] allowOrigins;
}
