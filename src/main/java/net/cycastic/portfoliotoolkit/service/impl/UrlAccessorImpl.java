package net.cycastic.portfoliotoolkit.service.impl;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.service.UrlAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UrlAccessorImpl implements UrlAccessor {
    private @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    public String getServiceBasePath() {
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
    public String getRequestPath() {
        var attrs = getAttributes();
        var request = attrs.getRequest();

        var url = request.getRequestURL();
        var query = request.getQueryString();

        if (query != null) {
            url.append('?').append(query);
        }

        return url.toString();
    }
}
