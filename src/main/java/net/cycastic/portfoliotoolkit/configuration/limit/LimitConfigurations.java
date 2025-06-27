package net.cycastic.portfoliotoolkit.configuration.limit;

import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.TypedUsageDetailsDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.limit")
public class LimitConfigurations {
    private TypedUsageDetailsDto[] limits;
}
