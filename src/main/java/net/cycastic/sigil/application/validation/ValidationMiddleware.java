package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationMiddleware implements Command.Middleware {
    private final ObjectProvider<CommandValidator> validators;

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        var validValidators = validators.stream().filter(v -> v.matches(command));
        try (validValidators){
            var it = validValidators.iterator();
            while (it.hasNext()){
                var validator = it.next();
                validator.validate(command);
            }
        }
        return next.invoke();
    }
}
