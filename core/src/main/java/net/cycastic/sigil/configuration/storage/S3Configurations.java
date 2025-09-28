package net.cycastic.sigil.configuration.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.s3")
public class S3Configurations extends BaseS3Configurations {
}
