package net.cycastic.sigil.application.misc.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class UUIDStringValidator implements ConstraintValidator<UUIDString, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isEmpty()){
            return true;
        }
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e){
            return false;
        }
    }
}
