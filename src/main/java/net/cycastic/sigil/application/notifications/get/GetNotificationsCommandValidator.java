package net.cycastic.sigil.application.notifications.get;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.dto.notifications.NotificationsDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

@Component
public class GetNotificationsCommandValidator implements CommandValidator<GetNotificationsCommand, NotificationsDto> {
    @Override
    public void validate(GetNotificationsCommand command) {
        if (command.getAmount() <= 0 || command.getAmount() > 2000){
            throw new RequestException(400, "Invalid amount");
        }
    }
}
