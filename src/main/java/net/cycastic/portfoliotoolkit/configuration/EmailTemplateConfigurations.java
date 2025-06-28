package net.cycastic.portfoliotoolkit.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.email.template")
public class EmailTemplateConfigurations {
    private @Nullable Long maxTemplateFileSize;
}
