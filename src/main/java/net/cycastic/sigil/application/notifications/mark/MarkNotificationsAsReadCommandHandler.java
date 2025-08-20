package net.cycastic.sigil.application.notifications.mark;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class MarkNotificationsAsReadCommandHandler implements Command.Handler<MarkNotificationsAsReadCommand, Void> {
    private static final Logger logger = LoggerFactory.getLogger(MarkNotificationsAsReadCommandHandler.class);

    private final UserService userService;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    @Override
    public Void handle(MarkNotificationsAsReadCommand command) {
        var user = userService.getUser();
        var amountUpdated = notificationRepository.markAsRead(user.getId(), command.getNotificationIds());
        if (command.getNotificationIds() != null && amountUpdated != command.getNotificationIds().length){
            logger.warn("Notification amount mismatched: expect {}, updated {}", command.getNotificationIds().length, amountUpdated);
        }

        if (amountUpdated > 0){
            notificationSender.sendNotification(user.getNotificationToken().toString(),
                    ApplicationConstants.NewNotificationEventType,
                    Collections.emptyList());
        }

        return null;
    }
}
