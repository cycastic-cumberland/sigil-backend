package net.cycastic.sigil.application.notifications.filter.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@Data
@TransactionalCommand
public class SaveNotificationSettingsCommand implements Command<Void> {
    @NotNull
    private String[] notificationType;
}
