package net.cycastic.sigil.service.serializer;

import java.io.InputStream;

public interface ObjectSerializer {
    String serialize(Object object);

    <T> T deserialize(String content, Class<T> klass);

    <T> T deserialize(InputStream inputStream, Class<T> klass);
}
