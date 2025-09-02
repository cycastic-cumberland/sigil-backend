package net.cycastic.sigil.application.misc.transaction;

import an.awesome.pipelinr.Command;
import lombok.SneakyThrows;
import org.hibernate.StaleObjectStateException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class RetryOnStaleMiddleware implements Command.Middleware {
    @Override
    public <R, C extends Command<R>> R invoke(C command, Command.Middleware.Next<R> next) {
        var retryAnn = AnnotatedElementUtils.findMergedAnnotation(command.getClass(), RetryOnStale.class);
        if (retryAnn == null) {
            return next.invoke();
        }

        final var maxAttempts = Math.max(1, retryAnn.maxAttempts());
        final var initialBackoffMs = Math.max(0, retryAnn.initialBackoff());
        final var multiplier = retryAnn.multiplier() <= 0 ? 2.0 : retryAnn.multiplier();
        final var maxBackoffMs = Math.max(initialBackoffMs, retryAnn.maxBackoff());

        var backoff = initialBackoffMs;
        RuntimeException lastException = null;
        for (var i = 0; i < maxAttempts; i++){
            try {
                return next.invoke();
            } catch (ObjectOptimisticLockingFailureException e){
                lastException = e;
                if (!(e.getRootCause() instanceof StaleObjectStateException)){
                    throw e;
                }

                sleep(jitter(backoff));
                backoff = Math.min(maxBackoffMs, (long) (backoff * multiplier));
            }
        }

        throw lastException;
    }

    @SneakyThrows
    private static void sleep(long millis) {
        if (millis <= 0) return;
        Thread.sleep(millis);
    }

    private static long jitter(long baseMs) {
        if (baseMs <= 0) return 0;
        // +/- 50% jitter
        double factor = 0.5 + Math.random();
        return (long) (baseMs * factor);
    }
}
