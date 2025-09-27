package net.cycastic.sigil.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.presigner")
public class SymmetricPresignerConfiguration extends ExtendibleKeyConfiguration{
}
