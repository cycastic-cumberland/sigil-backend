package net.cycastic.sigil.application.misc.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Indicate a String property is Base64 encoded.
 */
@Documented
@Constraint(validatedBy = Base64StringValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Base64String {
    /**
     * Minimum length of decoded array.
     */
    int min() default 0;

    /**
     * Maximum length of decoded array.
     */
    int max() default Integer.MAX_VALUE;

    /**
     * Validation message.
     */
    String message() default "Invalid Base64 string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
