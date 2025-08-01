package net.cycastic.sigil.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.cleaner")
public class CleanerConfigurations {
    private int incompleteUploadTtlSeconds;
}
