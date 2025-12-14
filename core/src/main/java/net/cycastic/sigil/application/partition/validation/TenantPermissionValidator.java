package net.cycastic.sigil.application.partition.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.application.validation.CommandValidator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TenantPermissionValidator implements CommandValidator {
    private final TenantService tenantService;

    @Override
    public void validate(Command command) {
        var annotation = Objects.requireNonNull(AnnotatedElementUtils.findMergedAnnotation(command.getClass(), TenantPermission.class));
        tenantService.checkPermission(annotation.value());
    }

    @Override
    public boolean matches(Class klass) {
        return AnnotatedElementUtils.findMergedAnnotation(klass, TenantPermission.class) != null;
    }
}
