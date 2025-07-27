package net.cycastic.sigil.service.impl;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.application.OriginConfigurations;
import net.cycastic.sigil.service.UrlAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UrlAccessorImpl implements UrlAccessor {
    private final OriginConfigurations originConfigurations;

    private static @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    private static String getBackendOriginFromServlet() {
        var attrs = getAttributes();
        var request = attrs.getRequest();
        var scheme = request.getScheme();
        var serverName = request.getServerName();
        var serverPort = request.getServerPort();
        var contextPath = request.getContextPath();

        return scheme + "://" + serverName
                + ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)
                ? (":" + serverPort)
                : "")
                + contextPath;
    }

    @Override
    public String getBackendOrigin() {
        var fromConfig = originConfigurations.getBackendOrigin();
        if (fromConfig != null){
            return fromConfig.endsWith("/") ? fromConfig.substring(0, fromConfig.length() - 1) : fromConfig;
        }

        return getBackendOriginFromServlet();
    }

    @Override
    public String getFrontendOrigin() {
        var fromConfig = originConfigurations.getFrontendOrigin();
        if (fromConfig != null){
            return fromConfig.endsWith("/") ? fromConfig.substring(0, fromConfig.length() - 1) : fromConfig;
        }

        // Assume that frontend and backend run on the same origin (probably through a proxy)
        return getBackendOrigin();
    }
}
