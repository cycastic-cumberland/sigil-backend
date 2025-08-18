package net.cycastic.sigil.application.notifications.remove;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteNotificationsCommandHandler implements Command.Handler<DeleteNotificationsCommand, Void> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteNotificationsCommandHandler.class);

    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;

    @Override
    public Void handle(DeleteNotificationsCommand command) {
        long amountUpdated;
        if (command.getNotificationIds() != null){
             amountUpdated = notificationRepository.removeNotifications(loggedUserAccessor.getUserId(), command.getNotificationIds());
        } else {
            amountUpdated = notificationRepository.removeNotifications(loggedUserAccessor.getUserId());
        }

        if (command.getNotificationIds() != null && amountUpdated != command.getNotificationIds().length){
            logger.warn("Notification amount mismatched: expect {}, updated {}", command.getNotificationIds().length, amountUpdated);
        }

        return null;
    }
}
