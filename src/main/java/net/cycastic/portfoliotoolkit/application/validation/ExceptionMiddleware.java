package net.cycastic.portfoliotoolkit.application.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.service.ExceptionConvertor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ExceptionMiddleware implements Command.Middleware {
    private final List<ExceptionConvertor> exceptionConvertors;

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        try {
            return next.invoke();
        } catch (Exception e){
            Throwable t = e;
            while (t != null) {
                for (var convertor: exceptionConvertors){
                    convertor.tryConvert(t);
                }
                t = t.getCause();
            }
            for (Throwable sup : e.getSuppressed()) {
                for (var convertor: exceptionConvertors){
                    convertor.tryConvert(sup);
                }
            }
            throw e;
        }
    }
}
