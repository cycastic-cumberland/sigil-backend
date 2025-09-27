package net.cycastic.sigil.domain.dto.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PusherAuthenticationDataDto {
    private String auth;

    @JsonProperty("channel_data")
    private String channelData;

    @JsonProperty("shared_secret")
    private String sharedSecret;
}
