package net.cycastic.sigil.application.misc.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Base64;

public class Base64StringValidator implements ConstraintValidator<Base64String, String> {
    private int min;
    private int max;

    @Override
    public void initialize(Base64String constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        try {
            var decoded = Base64.getDecoder().decode(value);
            int length = decoded.length;
            return length >= min && length < max;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

