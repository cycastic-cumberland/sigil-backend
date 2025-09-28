package net.cycastic.sigil.configuration.mail;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "application.email")
public class ApplicationEmailConfigurations extends MailSettings {
    private String encryptedPassword;

    private SqsRemoteEmailConfigurations sqs;
}
