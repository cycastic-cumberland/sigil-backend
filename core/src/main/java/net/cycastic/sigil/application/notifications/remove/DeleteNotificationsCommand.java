package net.cycastic.sigil.application.notifications.remove;

import an.awesome.pipelinr.Command;
import lombok.Data;
import jakarta.annotation.Nullable;

@Data
public class DeleteNotificationsCommand implements Command<Void> {
    @Nullable
    private long[] notificationIds;
}
