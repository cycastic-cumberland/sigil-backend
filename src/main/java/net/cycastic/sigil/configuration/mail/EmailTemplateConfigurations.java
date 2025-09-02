package net.cycastic.sigil.configuration.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.Nullable;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.email.template")
public class EmailTemplateConfigurations {
    private @Nullable Long maxTemplateFileSize;
}
