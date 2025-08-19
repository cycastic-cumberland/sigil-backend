package net.cycastic.sigil.application.notifications.send;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.user.validation.RequireAdmin;

import java.util.Map;

@Data
@RequireAdmin
public class SendNotificationToUserCommand implements Command<Void> {
    @Email
    private String userEmail;

    @NotNull
    private String notificationType;

    @NotNull
    private Map<String, Object> notificationContent;
}
