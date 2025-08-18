package net.cycastic.sigil.domain.dto.notifications;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.notification.Notification;

import java.time.OffsetDateTime;

@Data
@Builder
public class NotificationDto {
    private long id;
    private int userId;
    private boolean isRead;
    private String notificationContent;
    private String notificationType;
    private OffsetDateTime createdAt;

    public static NotificationDto fromDomain(Notification notification){
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .isRead(notification.isRead())
                .notificationContent(notification.getNotificationContent())
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
