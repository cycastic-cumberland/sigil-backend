package net.cycastic.sigil.application.misc.transaction;

import an.awesome.pipelinr.Command;
import lombok.SneakyThrows;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class RetryOnStaleMiddleware implements Command.Middleware {
    private static final Logger logger = LoggerFactory.getLogger(RetryOnStaleMiddleware.class);

    @Override
    public <R, C extends Command<R>> R invoke(C command, Command.Middleware.Next<R> next) {
        var retryAnn = AnnotatedElementUtils.findMergedAnnotation(command.getClass(), RetryOnStale.class);
        if (retryAnn == null) {
            return next.invoke();
        }

        var maxAttempts = Math.max(1, retryAnn.maxAttempts());
        var initialBackoffMs = Math.max(0, retryAnn.initialBackoff());
        var multiplier = retryAnn.multiplier() <= 0 ? 2.0 : retryAnn.multiplier();
        var maxBackoffMs = Math.max(initialBackoffMs, retryAnn.maxBackoff());

        var backoff = initialBackoffMs;
        RuntimeException lastException = null;
        for (var i = 0; i < maxAttempts; i++){
            if (i > 0){
                logger.debug("Attempt {}/{}", i + 1, maxAttempts);
            }
            try {
                return next.invoke();
            } catch (ObjectOptimisticLockingFailureException e){
                lastException = e;
                if (!(e.getRootCause() instanceof StaleObjectStateException)){
                    throw e;
                }
            }

            sleep(jitter(backoff));
            backoff = Math.min(maxBackoffMs, (long) (backoff * multiplier));
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
