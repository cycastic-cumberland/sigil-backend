package net.cycastic.sigil.service.impl.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper;

    public JacksonJsonSerializer(){
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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

    @Override
    @SneakyThrows
    public <T> T deserialize(InputStream inputStream, Class<T> klass) {
        return mapper.readValue(inputStream, klass);
    }
}
