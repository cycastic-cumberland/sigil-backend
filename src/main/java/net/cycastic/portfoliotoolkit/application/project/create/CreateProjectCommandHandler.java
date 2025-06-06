package net.cycastic.portfoliotoolkit.application.project.create;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.dto.IdDto;
import org.springframework.stereotype.Component;

@Component
public class CreateProjectCommandHandler implements Command.Handler<CreateProjectCommand, IdDto> {
    @Override
    public IdDto handle(CreateProjectCommand createProjectCommand) {
        throw new UnsupportedOperationException();
    }
}
