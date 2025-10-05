package net.cycastic.sigil.service.impl.serializer;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisJacksonSerializer<T> implements RedisSerializer<T> {
    private static final byte[] EMPTY_ARRAY = new byte[0];
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AnonymousSerializationData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Nullable
        private String className;

        @NotNull
        private byte[] jsonData;

        public byte[] getJsonData(){
            if (jsonData == null){
                return EMPTY_ARRAY;
            }

            return jsonData;
        }
    }

    private final JsonSerializer jsonSerializer;

    @Override
    public byte[] serialize(T value) throws SerializationException {
        byte[] output;
        try (var byteStream = new ByteArrayOutputStream();
             var objStream = new ObjectOutputStream(byteStream)){
            byte[] stringData;
            if (value != null){
                stringData = jsonSerializer.serialize(value).getBytes(StandardCharsets.UTF_8);
            } else {
                stringData = EMPTY_ARRAY;
            }
            var serializationData = AnonymousSerializationData.builder()
                    .className(value == null ? null : value.getClass().getName())
                    .jsonData(stringData)
                    .build();
            objStream.writeObject(serializationData);
            output = byteStream.toByteArray();
        } catch (IOException e){
            throw new SerializationException("Failed to serialize object", e);
        }

        return output;
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        try (var byteStream = new ByteArrayInputStream(bytes);
             var objStream = new ObjectInputStream(byteStream)){
            var serializationData = (AnonymousSerializationData)objStream.readObject();
            if (serializationData.getClassName() == null){
                return null;
            }

            var klass = Class.forName(serializationData.getClassName());
            return (T) jsonSerializer.deserialize(new String(serializationData.getJsonData(), StandardCharsets.UTF_8), klass);
        } catch (IOException | ClassNotFoundException e){
            throw new SerializationException("Failed to deserialize object", e);
        }
    }
}
