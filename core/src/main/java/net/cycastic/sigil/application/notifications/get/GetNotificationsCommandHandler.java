package net.cycastic.sigil.application.notifications.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.notifications.NotificationDto;
import net.cycastic.sigil.domain.dto.notifications.NotificationsDto;
import net.cycastic.sigil.domain.model.notification.Notification;
import net.cycastic.sigil.domain.model.notification.NotificationSetting;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.domain.repository.notifications.NotificationSettingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetNotificationsCommandHandler implements Command.Handler<GetNotificationsCommand, NotificationsDto> {
    private static final boolean[] FILTER_ALL_STATUSES = new boolean[]{ true, false };
    private static final boolean[] FILTER_READ_STATUS = new boolean[]{ true };
    private static final boolean[] FILTER_NOT_READ_STATUS = new boolean[]{ false };

    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Override
    public NotificationsDto handle(GetNotificationsCommand command) {
        var readStatuses = command.getIsRead() == null
                ? FILTER_ALL_STATUSES
                : command.getIsRead()
                    ? FILTER_READ_STATUS
                    : FILTER_NOT_READ_STATUS;
        Collection<String> filter = Collections.emptyList();
        if (command.isUseNotificationFilter()){
            filter = notificationSettingRepository.getDisabled(loggedUserAccessor.getUserId())
                    .stream()
                    .map(NotificationSetting::getNotificationType)
                    .toList();
        }

        Collection<Notification> notifications;
        if (command.isLower()){
            var n = notificationRepository.getNotificationLower(loggedUserAccessor.getUserId(),
                    Objects.requireNonNullElse(command.getSinceId(), Long.MAX_VALUE),
                    readStatuses,
                    filter,
                    command.getAmount());
            Collections.reverse(n);
            notifications = n;
        } else {
            notifications = notificationRepository.getNotificationUpper(loggedUserAccessor.getUserId(),
                    Objects.requireNonNullElse(command.getSinceId(), 0L),
                    readStatuses,
                    filter,
                    command.getAmount());
        }

        return NotificationsDto.builder()
                .notifications(notifications.stream()
                        .map(NotificationDto::fromDomain)
                        .toList())
                .build();
    }
}
