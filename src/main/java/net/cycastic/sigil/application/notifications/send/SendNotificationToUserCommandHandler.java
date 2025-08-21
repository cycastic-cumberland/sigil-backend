package net.cycastic.sigil.application.notifications.send;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.notifications.NotificationService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SendNotificationToUserCommandHandler implements Command.Handler<SendNotificationToUserCommand, Void> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final NotificationSender notificationSender;
    private final NotificationService notificationService;
    private final TaskExecutor taskScheduler;

    @Override
    @SneakyThrows
    public Void handle(SendNotificationToUserCommand command) {
        var user = userRepository.getByEmail(command.getUserEmail())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var notificationContent = OBJECT_MAPPER.writeValueAsString(command.getNotificationContent());
        notificationService.saveNotification(user, command.getNotificationType(), notificationContent);

        var notificationToken = user.getNotificationToken();
        taskScheduler.execute(() -> notificationSender.sendNotification(notificationToken.toString(),
                ApplicationConstants.NewNotificationEventType,
                Collections.emptyList()));
        return null;
    }
}
