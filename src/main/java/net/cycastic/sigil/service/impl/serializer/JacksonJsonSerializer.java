package net.cycastic.sigil.service.impl.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

@Component
public class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public String serialize(Object object) {
        return mapper.writeValueAsString(object);
    }

    @Override
    @SneakyThrows
    public <T> T deserialize(String content, Class<T> klass) {
        return mapper.readValue(content, klass);
    }
}
