package net.cycastic.sigil.application.partition.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Users who execute this command must have specific partition permission.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PartitionPermission {
    /**
     * Permission mask.
     */
    int value() default 0;
}
