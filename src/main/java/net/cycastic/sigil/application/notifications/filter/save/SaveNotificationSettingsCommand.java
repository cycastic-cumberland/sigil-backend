package net.cycastic.sigil.application.notifications.filter.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveNotificationSettingsCommand implements Command<Void> {
    @NotNull
    private String[] notificationType;
}
