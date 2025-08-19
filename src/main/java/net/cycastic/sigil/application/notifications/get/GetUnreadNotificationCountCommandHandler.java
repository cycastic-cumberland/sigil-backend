package net.cycastic.sigil.application.notifications.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.CountDto;
import net.cycastic.sigil.domain.model.notification.NotificationSetting;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.domain.repository.notifications.NotificationSettingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class GetUnreadNotificationCountCommandHandler implements Command.Handler<GetUnreadNotificationCountCommand, CountDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Override
    public CountDto handle(GetUnreadNotificationCountCommand command) {
        Collection<String> filter = Collections.emptyList();
        if (command.isUseNotificationFilter()){
            filter = notificationSettingRepository.getDisabled(loggedUserAccessor.getUserId())
                    .stream()
                    .map(NotificationSetting::getNotificationType)
                    .toList();
        }

        var count = notificationRepository.countUnreadNotifications(loggedUserAccessor.getUserId(), filter);
        return new CountDto(count);
    }
}
