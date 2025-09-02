package net.cycastic.sigil.configuration.security;

import lombok.Data;
import jakarta.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
public class ExtendibleKeyConfiguration {
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
