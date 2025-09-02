package net.cycastic.sigil.application.misc.transaction;

import java.lang.annotation.*;

/**
 * Retry the command on stale state (optimistic concurrency failure).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RetryOnStale {
    /**
     * Max attempt count.
     */
    int maxAttempts() default 5;

    /**
     * Initial backoff (milliseconds).
     */
    long initialBackoff() default 50;

    /**
     * Max backoff (milliseconds).
     */
    long maxBackoff() default 1000;

    /**
     * Backoff multiplier.
     */
    double multiplier() default 2.0;
}
