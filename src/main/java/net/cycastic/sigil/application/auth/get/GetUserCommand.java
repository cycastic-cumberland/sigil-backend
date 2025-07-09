package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.UserDto;

@Data
public class GetUserCommand implements Command<UserDto> {
    private String userEmail;
}
