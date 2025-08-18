package net.cycastic.sigil.application.notifications.mark;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarkNotificationsAsReadCommandHandler implements Command.Handler<MarkNotificationsAsReadCommand, Void> {
    private static final Logger logger = LoggerFactory.getLogger(MarkNotificationsAsReadCommandHandler.class);

    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;

    @Override
    public Void handle(MarkNotificationsAsReadCommand command) {
        var amountUpdated = notificationRepository.markAsRead(loggedUserAccessor.getUserId(), command.getNotificationIds());
        if (amountUpdated != command.getNotificationIds().length){
            logger.warn("Notification amount mismatched: expect {}, updated {}", command.getNotificationIds().length, amountUpdated);
        }

        return null;
    }
}
