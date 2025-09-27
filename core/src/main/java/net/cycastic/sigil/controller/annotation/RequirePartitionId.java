package net.cycastic.sigil.controller.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import net.cycastic.sigil.domain.ApplicationConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(parameters = {
        @Parameter(
                name = ApplicationConstants.PARTITION_ID_HEADER,
                in = ParameterIn.HEADER,
                required = true,
                description = "Requires partition ID"
        )
})
public @interface RequirePartitionId {
}
