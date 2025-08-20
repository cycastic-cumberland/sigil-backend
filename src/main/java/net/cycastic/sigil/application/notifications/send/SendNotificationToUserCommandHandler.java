package net.cycastic.sigil.application.notifications.send;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.notification.Notification;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SendNotificationToUserCommandHandler implements Command.Handler<SendNotificationToUserCommand, Void> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final NotificationSender notificationSender;
    private final NotificationRepository notificationRepository;

    @Override
    @SneakyThrows
    @Transactional
    public Void handle(SendNotificationToUserCommand command) {
        var user = userRepository.getByEmail(command.getUserEmail())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var notificationContent = OBJECT_MAPPER.writeValueAsString(command.getNotificationContent());
        var notification = Notification.builder()
                .user(user)
                .isRead(false)
                .notificationContent(notificationContent)
                .notificationType(command.getNotificationType())
                .createdAt(OffsetDateTime.now())
                .build();
        notificationRepository.save(notification);
        notificationSender.sendNotification(user.getNotificationToken().toString(),
                ApplicationConstants.NewNotificationEventType,
                Collections.emptyList());
        return null;
    }
}
