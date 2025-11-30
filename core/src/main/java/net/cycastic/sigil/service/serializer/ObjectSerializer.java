package net.cycastic.sigil.service.serializer;

import java.io.InputStream;
import java.io.OutputStream;

public interface ObjectSerializer {
    String serialize(Object object);

    <T> void serialize(T value, OutputStream outputStream, Class<T> klass);

    <T> T deserialize(String content, Class<T> klass);

    <T> T deserialize(InputStream inputStream, Class<T> klass);
}
