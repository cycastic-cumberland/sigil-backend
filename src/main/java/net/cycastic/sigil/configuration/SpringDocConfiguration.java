package net.cycastic.sigil.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import net.cycastic.sigil.controller.annotation.RequireProjectId;
import net.cycastic.sigil.controller.annotation.UseSymmetricKey;
import net.cycastic.sigil.domain.ApplicationConstants;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.Operation;

@Configuration
public class SpringDocConfiguration {
    @SecurityScheme(
            name = "Bearer",
            type = SecuritySchemeType.HTTP,
            scheme = "bearer",
            bearerFormat = "JWT"
    )
    @Configuration
    public static class BearerAuthenticationConfiguration {
        @Bean
        public OpenAPI customOpenAPI() {
            return new OpenAPI()
                    .components(
                            new Components()
                                    .addSecuritySchemes("Bearer",
                                            new io.swagger.v3.oas.models.security.SecurityScheme()
                                                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                    .scheme("bearer")
                                                    .bearerFormat("JWT")
                                    )
                    )
                    .addSecurityItem(new SecurityRequirement().addList("Bearer"));
        }
    }

    @Bean
    public OperationCustomizer requireHeaderWhenAnnotated() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            var controllerClass = handlerMethod.getBeanType();
            if (controllerClass.isAnnotationPresent(RequireProjectId.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(RequireProjectId.class)) {
                var header = new Parameter()
                        .in("header")
                        .name(ApplicationConstants.TENANT_ID_HEADER)
                        .required(true)
                        .schema(new StringSchema())
                        .description("Requires project ID");
                operation.addParametersItem(header);
            }
            if (controllerClass.isAnnotationPresent(UseSymmetricKey.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(UseSymmetricKey.class)) {
                var header = new Parameter()
                        .in("header")
                        .name(ApplicationConstants.SYMMETRIC_KEY_HEADER)
                        .required(false)
                        .schema(new StringSchema())
                        .description("Symmetric encryption key");
                operation.addParametersItem(header);
            }
            return operation;
        };
    }
}