package net.cycastic.sigil.configuration.aws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.aws")
public class AwsConfigurations {
    private String accessKey;
    private String secretKey;

    @Bean
    public static AwsCredentialsProvider awsCredentialsProvider(AwsConfigurations configurations){
        if (configurations.accessKey == null || configurations.accessKey.isEmpty()){
            return DefaultCredentialsProvider.create();
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(configurations.getAccessKey(),
                configurations.getSecretKey()));
    }
}
