package net.cycastic.portfoliotoolkit.application.project.createproject;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.dto.IdDto;

public class CreateProjectCommandHandler implements Command.Handler<CreateProjectCommand, IdDto> {
    @Override
    public IdDto handle(CreateProjectCommand createProjectCommand) {
        throw new UnsupportedOperationException();
    }
}
