package net.cycastic.sigil.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "application.exceptions")
public class ExceptionHandlerConfigurations {
    private boolean showStackTrace;
}
