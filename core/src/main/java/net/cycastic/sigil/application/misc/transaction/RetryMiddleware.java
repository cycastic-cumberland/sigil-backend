package net.cycastic.sigil.application.misc.transaction;

import an.awesome.pipelinr.Command;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.misc.OncePerRequestMiddleware;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Order(2)
@Component
public class RetryMiddleware extends OncePerRequestMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(RetryMiddleware.class);

    private static void matchException(Set<Retry.Event> onEvents, RuntimeException e){
        var backoff = false;
        if (onEvents.contains(Retry.Event.STALE) && e instanceof ObjectOptimisticLockingFailureException o){
            if (o.getRootCause() instanceof StaleObjectStateException){
                logger.debug("Backoff on StaleObjectStateException");
                backoff = true;
            }
        }
        if (onEvents.contains(Retry.Event.INTEGRITY_VIOLATION) && e instanceof DataIntegrityViolationException d){
            if (d.getRootCause() instanceof SQLIntegrityConstraintViolationException){
                logger.debug("Backoff on SQLIntegrityConstraintViolationException");
                backoff = true;
            }
        }

        if (!backoff){
            throw e;
        }
    }

    @Override
    public <R, C extends Command<R>> R invokeInternal(C command, Command.Middleware.Next<R> next) {
        var retryAnn = AnnotatedElementUtils.findMergedAnnotation(command.getClass(), Retry.class);
        if (retryAnn == null) {
            return next.invoke();
        }

        var eventSet = Arrays.stream(retryAnn.value())
                .collect(Collectors.toSet());
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
            } catch (RuntimeException e){
                lastException = e;
                if (eventSet.isEmpty()){
                    throw e;
                }
                matchException(eventSet, e);
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
