package net.cycastic.sigil.application.notifications.send;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.notifications.NotificationService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.notification.NotificationSender;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SendNotificationToUserCommandHandler implements Command.Handler<SendNotificationToUserCommand, Void> {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TaskExecutor taskScheduler;
    private final JsonSerializer jsonSerializer;

    @Override
    @SneakyThrows
    public Void handle(SendNotificationToUserCommand command) {
        var user = userRepository.getByEmail(command.getUserEmail())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        var notificationContent = jsonSerializer.serialize(command.getNotificationContent());
        notificationService.saveNotification(user, command.getNotificationType(), notificationContent);

        var notificationToken = user.getNotificationToken();
        taskScheduler.execute(() -> notificationService.triggerNotification(notificationToken.getToken(),
                ApplicationConstants.NewNotificationEventType));
        return null;
    }
}
