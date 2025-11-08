package net.cycastic.sigil.service.serializer;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSerializer extends ObjectSerializer{
    <T> T deserialize(JsonNode jsonNode, Class<T> klass);
}
