package net.cycastic.sigil.service.serializer;

public interface ObjectSerializer {
    String serialize(Object object);

    <T> T deserialize(String content, Class<T> klass);
}
