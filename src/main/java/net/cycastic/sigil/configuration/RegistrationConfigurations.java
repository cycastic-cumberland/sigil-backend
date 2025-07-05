package net.cycastic.sigil.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.auth.registration")
public class RegistrationConfigurations {
    private long registrationLinkValidSeconds;
    private int resendVerificationLimitSeconds;
}
