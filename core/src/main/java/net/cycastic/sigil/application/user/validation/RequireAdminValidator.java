package net.cycastic.sigil.application.user.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@RequiredArgsConstructor
public class RequireAdminValidator implements CommandValidator {
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public void validate(Command command) {
        if (!loggedUserAccessor.isAdmin()){
            throw RequestException.forbidden();
        }
    }

    @Override
    public boolean matches(Class klass) {
        return AnnotatedElementUtils.findMergedAnnotation(klass, RequireAdmin.class) != null;
    }
}
