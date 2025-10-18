package net.cycastic.sigil.application.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.exception.ValidationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JakartaValidationHelper {
    private record ValidationError(String field, String message){}

    private static ValidationError toValidationError(MessageSourceResolvable messageSourceResolvable){
        if (messageSourceResolvable instanceof FieldError fieldError){
            return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ValidationError("*", messageSourceResolvable.getDefaultMessage());
    }

    private static ValidationError toValidationError(String formattedMessage){
        var index = formattedMessage.indexOf(':');
        if (index < 0 || index == formattedMessage.length() - 1){
            return new ValidationError("*", formattedMessage);
        }

        return new ValidationError(formattedMessage.substring(0, index), formattedMessage.substring(index + 1).trim());
    }

    private static <T> Map<String, List<String>> groupViolationsByProperty(
            Set<ConstraintViolation<T>> violations) {

        return violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(
                                ConstraintViolation::getMessage,
                                Collectors.toList()
                        )
                ));
    }

    public static <T> void validateObject(T object){
        if (object == null){
            throw RequestException.withExceptionCode("C400T015");
        }

        try (var factory = Validation.buildDefaultValidatorFactory()){
            var validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(object);
            if (!violations.isEmpty()){
                throw new ValidationException(groupViolationsByProperty(violations));
            }
        }
    }

    public static Map<String, List<String>> toValidationMap(@NotNull MethodValidationResult result) {
        return result.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(JakartaValidationHelper::toValidationError)
                .collect(Collectors.groupingBy(
                        ValidationError::field,
                        Collectors.mapping(
                                ValidationError::message,
                                Collectors.toList()
                        )
                ));
    }

    public static Map<String, List<String>> toValidationMap(@NotNull MethodArgumentNotValidException ex){
        return Arrays.stream(ex.getDetailMessageArguments())
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(JakartaValidationHelper::toValidationError)
                .collect(Collectors.groupingBy(
                        ValidationError::field,
                        Collectors.mapping(
                                ValidationError::message,
                                Collectors.toList()
                        )
                ));
    }
}
