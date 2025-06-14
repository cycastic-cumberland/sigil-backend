package net.cycastic.portfoliotoolkit.application.auth.self;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.dto.UserDto;

public class GetSelfCommand implements Command<UserDto> {
    public static final GetSelfCommand INSTANCE = new GetSelfCommand();
}
