package net.cycastic.sigil.application.misc.transaction;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Order // Integer.MAX_VALUE
@Component
@RequiredArgsConstructor
public class TransactionalCommandMiddleware implements Command.Middleware {
    private final PlatformTransactionManager txManager;

    @Override
    public <R, C extends Command<R>> R invoke(C command, Command.Middleware.Next<R> next) {
        var txAnn = AnnotatedElementUtils.findMergedAnnotation(command.getClass(), TransactionalCommand.class);

        if (txAnn == null) {
            return next.invoke();
        }

        var template = new TransactionTemplate(txManager);
        template.setPropagationBehavior(txAnn.propagation().value());
        template.setIsolationLevel(txAnn.isolation().value());
        template.setReadOnly(txAnn.readOnly());
        template.setTimeout(txAnn.timeout());

        return template.execute(status -> next.invoke());
    }
}
