package net.cycastic.sigil.application.misc.transaction;

import java.lang.annotation.*;

/**
 * Indicating a command is retryable.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Retry {
    /**
     * Constants for retryable events.
     */
    enum Event {
        /**
         * Retry on stale object state.
         */
        STALE,

        /**
         * Retry on integrity violation.
         */
        INTEGRITY_VIOLATION
    }

    /**
     * Events to retry upon;
     */
    Event[] value();

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
