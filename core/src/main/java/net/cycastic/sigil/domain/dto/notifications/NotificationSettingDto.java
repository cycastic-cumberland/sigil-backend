package net.cycastic.sigil.domain.dto.notifications;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.notification.NotificationSetting;

@Data
@Builder
public class NotificationSettingDto {
    private int userId;

    private String notificationType;

    private boolean notificationDisabled;

    public static NotificationSettingDto fromDomain(NotificationSetting notificationSetting){
        return NotificationSettingDto.builder()
                .userId(notificationSetting.getUser().getId())
                .notificationType(notificationSetting.getNotificationType())
                .notificationDisabled(notificationSetting.isNotificationDisabled())
                .build();
    }
}
