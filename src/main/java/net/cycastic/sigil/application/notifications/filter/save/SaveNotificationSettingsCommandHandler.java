package net.cycastic.sigil.application.notifications.filter.save;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.SimpleDiffUtilities;
import net.cycastic.sigil.domain.model.notification.NotificationSetting;
import net.cycastic.sigil.domain.repository.notifications.NotificationSettingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SaveNotificationSettingsCommandHandler implements Command.Handler<SaveNotificationSettingsCommand, Void> {
    private record SettingKey(int userId, String notificationType){}

    private static final SimpleDiffUtilities<NotificationSetting, SettingKey> DIFF = new SimpleDiffUtilities<>(SaveNotificationSettingsCommandHandler::compareIdentity,
            SaveNotificationSettingsCommandHandler::compareData);

    private final UserService userService;
    private final NotificationSettingRepository notificationSettingRepository;

    private static SettingKey compareIdentity(NotificationSetting setting){
        return new SettingKey(setting.getUser().getId(), setting.getNotificationType());
    }

    private static boolean compareData(NotificationSetting lhs, NotificationSetting rhs){
        return false;
    }

    @Override
    @Transactional
    public Void handle(SaveNotificationSettingsCommand command) {
        var user = userService.getUser();
        var currentFilters = notificationSettingRepository.findByUser_Id(user.getId(), Pageable.unpaged());
        var newFilters = Arrays.stream(command.getNotificationType())
                .map(s -> NotificationSetting.builder()
                        .user(user)
                        .notificationType(s)
                        .notificationDisabled(true)
                        .build())
                .toList();
        var diff = DIFF.shallowDiff(currentFilters, newFilters);
        notificationSettingRepository.saveAll(diff.getNewEntities());
        notificationSettingRepository.deleteAll(diff.getDeletedEntities());

        return null;
    }
}
