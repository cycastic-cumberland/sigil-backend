package net.cycastic.portfoliotoolkit.application.sample.helloworld;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelloWorldCommandHandler implements Command.Handler<HelloWorldCommand, String> {
    private final LoggedUserAccessor userAccessor;

    @Override
    public String handle(HelloWorldCommand helloWorldCommand) {
        return String.format("Your project ID is %d", userAccessor.getProjectId());
    }
}
