package net.cycastic.sigil.configuration.application;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.origins")
public class OriginConfigurations {
    @Nullable
    private String backendOrigin;

    @Nullable
    private String frontendOrigin;
}
