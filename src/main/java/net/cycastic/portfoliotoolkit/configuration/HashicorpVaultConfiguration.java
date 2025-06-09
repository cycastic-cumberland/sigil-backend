package net.cycastic.portfoliotoolkit.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.vault")
public class HashicorpVaultConfiguration {
    private String apiAddress;
    private String token;
    private String keyName;
    private int apiVersion = 1;

    public boolean isValid(){
        return (apiAddress != null && !apiAddress.isEmpty()) &&
                (token != null && !token.isEmpty()) &&
                (keyName != null && !keyName.isEmpty()) &&
                apiVersion > 0;
    }
}
