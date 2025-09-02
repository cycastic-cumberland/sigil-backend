package net.cycastic.sigil.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Order(1)
@Component
@RequiredArgsConstructor
public class ValidationMiddleware implements Command.Middleware {
    private final ConcurrentHashMap<Class, Collection<CommandValidator>> validatorMap = new ConcurrentHashMap<>();
    private final ObjectProvider<CommandValidator> validators;

    private Collection<CommandValidator> matchAllValidators(Class klass){
        return validators.stream()
                .filter(v -> v.matches(klass))
                .toList();
    }

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        var validators = validatorMap.computeIfAbsent(command.getClass(), this::matchAllValidators);
        for (var validator : validators){
            validator.validate(command);
        }
        return next.invoke();
    }
}
