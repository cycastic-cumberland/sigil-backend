package net.cycastic.sigil.application.notifications.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.CountDto;

@Data
public class GetUnreadNotificationCountCommand implements Command<CountDto> {
}
