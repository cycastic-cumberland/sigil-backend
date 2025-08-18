package net.cycastic.sigil.application.notifications.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.CountDto;
import net.cycastic.sigil.domain.repository.notifications.NotificationRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUnreadNotificationCountCommandHandler implements Command.Handler<GetUnreadNotificationCountCommand, CountDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationRepository notificationRepository;
    @Override
    public CountDto handle(GetUnreadNotificationCountCommand command) {
        var count = notificationRepository.countByUser_IdAndRead(loggedUserAccessor.getUserId(), false);
        return new CountDto(count);
    }
}
