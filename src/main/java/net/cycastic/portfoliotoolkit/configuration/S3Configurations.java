package net.cycastic.portfoliotoolkit.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.s3")
public class S3Configurations {
    private String regionName;
    private String accessKey;
    private String secretKey;
    private String attachmentBucketName;
    private String serviceUrl;
}
