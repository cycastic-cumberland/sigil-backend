package net.cycastic.sigil.application.notifications.filter.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.notifications.NotificationSettingsDto;

@Data
public class GetNotificationSettingsCommand implements Command<NotificationSettingsDto> {
}
