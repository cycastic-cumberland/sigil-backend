package net.cycastic.sigil.application.notifications.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.notifications.NotificationsDto;
import jakarta.annotation.Nullable;

@Data
public class GetNotificationsCommand implements Command<NotificationsDto> {
    private int amount;

    @Nullable
    private Long sinceId;

    @Nullable
    private Boolean isRead;

    private boolean lower;

    private boolean useNotificationFilter;
}
