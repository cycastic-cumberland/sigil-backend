package net.cycastic.sigil.application.notifications.remove;

import an.awesome.pipelinr.Command;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class DeleteNotificationsCommand implements Command<Void> {
    @Nullable
    private long[] notificationIds;
}
