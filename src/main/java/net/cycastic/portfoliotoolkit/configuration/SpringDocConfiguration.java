package net.cycastic.portfoliotoolkit.configuration;

import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.Operation;

@Configuration
public class SpringDocConfiguration {
    @Bean
    public OperationCustomizer requireHeaderWhenAnnotated() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Class<?> controllerClass = handlerMethod.getBeanType();
            if (controllerClass.isAnnotationPresent(RequireProjectId.class)) {
                Parameter header = new Parameter()
                        .in("header")
                        .name(ApplicationConstants.PROJECT_ID_HEADER)
                        .required(true)
                        .schema(new StringSchema())
                        .description("Requires project ID");
                operation.addParametersItem(header);
            }
            return operation;
        };
    }
}