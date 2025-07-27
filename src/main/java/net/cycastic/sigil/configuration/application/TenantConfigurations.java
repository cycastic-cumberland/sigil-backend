package net.cycastic.sigil.configuration.application;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.tenants")
public class TenantConfigurations {
    private long invitationLinkValidSeconds;
    private int resendInvitationLimitSeconds;
}
