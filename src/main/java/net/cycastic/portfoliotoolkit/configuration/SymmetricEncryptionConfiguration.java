package net.cycastic.portfoliotoolkit.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.encryption")
public class SymmetricEncryptionConfiguration {
    private String key;
    private String keyBase64;
    private String saltBase64;

    public @Nullable byte[] getIkm(){
        if (key != null && !key.isEmpty()){
            return key.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBase64 != null && !keyBase64.isEmpty()){
            return Base64.getDecoder().decode(keyBase64.getBytes(StandardCharsets.UTF_8));
        }

        return null;
    }

    public @Nullable byte[] getSalt(){
        if (saltBase64 != null){
            return Base64.getDecoder().decode(saltBase64.getBytes(StandardCharsets.UTF_8));
        }

        return null;
    }

    public boolean isValid(){
        return (key != null && !key.isEmpty()) || (keyBase64 != null && !keyBase64.isEmpty());
    }
}
