package net.cycastic.portfoliotoolkit.domain;

import lombok.SneakyThrows;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionStorage implements DisposableBean {
    private final Map<String,Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        var value = data.get(key);
        if (value == null){
            return null;
        }

        if (!type.isInstance(value)){
            return null;
        }

        return type.cast(value);
    }

    @Override
    @SneakyThrows
    public void destroy() {
        if (data.isEmpty()){
            return;
        }
        for (var keyPair : data.entrySet()){
            if (!(keyPair.getValue() instanceof AutoCloseable)){
                continue;
            }

            ((AutoCloseable)keyPair.getValue()).close();
        }

        data.clear();
    }
}
