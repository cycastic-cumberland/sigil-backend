package net.cycastic.sigil.application.admin.user.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Min;
import lombok.Data;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.UserDto;

@Data
@RequireAdmin
public class GetUserByIdCommand implements Command<UserDto> {
    @Min(1)
    private int id;
}
