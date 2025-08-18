package net.cycastic.sigil.domain.dto.notifications;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class NotificationsDto {
    private Collection<NotificationDto> notifications;
}
