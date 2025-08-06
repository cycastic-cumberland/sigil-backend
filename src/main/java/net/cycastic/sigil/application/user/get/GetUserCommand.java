package net.cycastic.sigil.application.user.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.domain.dto.UserDto;

@Data
public class GetUserCommand implements Command<UserDto> {
    @NotNull
    @Email
    private String userEmail;
}
