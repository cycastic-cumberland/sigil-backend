package net.cycastic.sigil.application.misc.transaction;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import java.lang.annotation.*;

/**
 * Wrap the command handler inside a transaction.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalCommand {
    /**
     * Mirrors @Transactional#propagation
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * Mirrors @Transactional#isolation
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Mirrors @Transactional#readOnly
     */
    boolean readOnly() default false;

    /**
     * Mirrors @Transactional#timeout
     */
    int timeout() default -1; // TransactionDefinition.TIMEOUT_DEFAULT
}
