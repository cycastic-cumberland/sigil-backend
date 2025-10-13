package net.cycastic.sigil.service.job;

import an.awesome.pipelinr.repack.com.google.common.reflect.TypeToken;

public interface BackgroundJobHandler<T extends BackgroundJob> {
    default boolean matches(Class<T> klass) {
        var typeToken = new TypeToken<T>(getClass()){};

        return typeToken.isSupertypeOf(klass);
    }

    void process(T data);
}
