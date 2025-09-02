package net.cycastic.sigil.application.notifications.mark;

import an.awesome.pipelinr.Command;
import lombok.Data;
import jakarta.annotation.Nullable;

@Data
public class MarkNotificationsAsReadCommand implements Command<Void> {
    @Nullable
    private long[] notificationIds;
}
