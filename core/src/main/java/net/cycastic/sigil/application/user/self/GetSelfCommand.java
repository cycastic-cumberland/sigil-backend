package net.cycastic.sigil.application.user.self;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.UserDto;

public class GetSelfCommand implements Command<UserDto> {
    public static final GetSelfCommand INSTANCE = new GetSelfCommand();
}
