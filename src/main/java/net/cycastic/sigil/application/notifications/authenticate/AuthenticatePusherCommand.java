package net.cycastic.sigil.application.notifications.authenticate;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.application.misc.annotation.UUIDString;
import net.cycastic.sigil.domain.dto.notifications.PusherAuthenticationDataDto;

@Data
@Builder
public class AuthenticatePusherCommand implements Command<PusherAuthenticationDataDto> {
    private String socketId;

    private String channelName;

    @NotEmpty
    @UUIDString
    public String getNotificationTokenString(){
        if (channelName == null){
            return null;
        }
        if (channelName.startsWith("private-")){
            return channelName.substring("private-".length());
        }

        return channelName;
    }
}
