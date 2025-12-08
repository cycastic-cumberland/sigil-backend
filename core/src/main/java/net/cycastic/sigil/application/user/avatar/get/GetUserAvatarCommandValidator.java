package net.cycastic.sigil.application.user.avatar.get;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.InputStreamResponse;
import org.springframework.stereotype.Component;

@Component
public class GetUserAvatarCommandValidator implements CommandValidator<GetUserAvatarCommand, InputStreamResponse> {
    @Override
    public void validate(GetUserAvatarCommand command) {
        if (command.getSize() != null && command.getSize() % 100 != 0){
            throw new RequestException(400, "Avatar size must be dividable by 100");
        }
    }
}
