package net.cycastic.portfoliotoolkit.service.impl;

import lombok.NonNull;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoggedUserAccessorImpl implements LoggedUserAccessor {
    private @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    public int getProjectId() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.PROJECT_ID_HEADER);
        if (header == null){
            throw new RequestException(400, String.format("No %s header found", ApplicationConstants.PROJECT_ID_HEADER));
        }

        var opt = ApplicationUtilities.tryParseInt(header);
        if (opt.isEmpty()){
            throw new RequestException(400, "Failed to parse project ID");
        }

        return opt.get();
    }
}
