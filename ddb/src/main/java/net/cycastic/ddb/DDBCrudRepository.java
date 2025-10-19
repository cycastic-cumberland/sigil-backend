package net.cycastic.ddb;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.ddb.annotation.PK;
import net.cycastic.ddb.annotation.SK;
import net.cycastic.ddb.annotation.TTL;
import net.cycastic.ddb.repository.CrudRepository;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DDBCrudRepository<T, TPK, TSK> implements CrudRepository<T, TPK, TSK> {
    private static class DoubleSortOrder{
        public static byte[] toSortableBytes(double value) {
            long bits = Double.doubleToRawLongBits(value);
            if (bits < 0) {
                bits ^= 0x7fffffffffffffffL;
            } else {
                bits ^= 0x8000000000000000L;
            }
            return ByteBuffer.allocate(8).putLong(bits).array();
        }

        public static double fromSortableBytes(byte[] bytes) {
            long bits = ByteBuffer.wrap(bytes).getLong();
            if ((bits & 0x8000000000000000L) != 0) {
                bits ^= 0x8000000000000000L;
            } else {
                bits ^= 0x7fffffffffffffffL;
            }
            return Double.longBitsToDouble(bits);
        }
    }

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    private static boolean isPublicGetter(String fieldName, Class<?> fieldType, Set<String> methodNames){
        if (fieldName.isEmpty()){
            return false;
        }
        if (fieldName.startsWith("is") && boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)){
            fieldName = fieldName.substring(2);
        } else {
            fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }

        if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)){
            if (methodNames.contains("is" + fieldName)){
                return true;
            }
        }
        return methodNames.contains("get" + fieldName);
    }

    private static boolean isPublicSetter(String fieldName, Class<?> fieldType, Set<String> methodNames){
        if (fieldName.isEmpty()){
            return false;
        }
        if (fieldName.length() >= 3 &&
                fieldName.startsWith("is") &&
                Character.isUpperCase(fieldName.charAt(2)) &&
                boolean.class.equals(fieldType) ||
                Boolean.class.equals(fieldType)){
            fieldName = fieldName.substring(2);
        } else {
            fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }

        return methodNames.contains("set" + fieldName);
    }

    private static AttributeValue mapCollectionAttribute(Set<Object> walked, Object object){
        if (object == null){
            return AttributeValue.fromNul(true);
        }
        if (object instanceof String || object instanceof OffsetDateTime || object instanceof Collection ||
                object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double){
            var value = toAttribute(walked, object);
            return AttributeValue.fromM(Map.of("$class", AttributeValue.fromS(object.getClass().getName()),
                    "$value", value));
        }

        return toAttribute(walked, object);
    }

    private static AttributeValue toAttribute(Set<Object> walked, Object object){
        if (object == null){
            return AttributeValue.fromNul(true);
        }
        if (object instanceof String s){
            return AttributeValue.fromS(s);
        }
        if (object instanceof OffsetDateTime o){
            return AttributeValue.fromS(o.toString());
        }
        if (object instanceof Boolean b){
            return AttributeValue.fromBool(b);
        }
        if (object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double){
            return AttributeValue.fromN(object.toString());
        }
        if (object instanceof Collection<?> collection){
            return AttributeValue.fromL(collection.stream()
                    .map(o -> mapCollectionAttribute(walked, o))
                    .toList());
        }
        var map = serialize(walked, object, (Class<? super Object>) object.getClass());
        return AttributeValue.fromM(map);
    }

    private static long toEpochTtl(Object object){
        long epoch;
        switch (object) {
            case String string -> {
                var date = OffsetDateTime.parse(string);
                epoch = date.toEpochSecond();
            }
            case OffsetDateTime offsetDateTime -> epoch = offsetDateTime.toEpochSecond();
            case Integer integer -> epoch = integer;
            case Long l -> epoch = l;
            case null, default -> throw new IllegalStateException("Object type is unfit to be TTL field");
        }

        return epoch;
    }

    private static AttributeValue toSk(Object object){
        switch (object) {
            case String s -> {
                return AttributeValue.fromB(SdkBytes.fromString(s, StandardCharsets.UTF_8));
            }
            case byte[] b -> {
                return AttributeValue.fromB(SdkBytes.fromByteArrayUnsafe(b));
            }
            case OffsetDateTime o -> {
                var l = toEpochTtl(o);
                return toSk(l);
            }
            case Integer i -> {
                var bb = ByteBuffer.allocate(Integer.BYTES);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.putInt(i);
                return AttributeValue.fromB(SdkBytes.fromByteArray(bb.array()));
            }
            case Long l -> {
                var bb = ByteBuffer.allocate(Long.BYTES);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.putLong(l);
                return AttributeValue.fromB(SdkBytes.fromByteArray(bb.array()));
            }
            case Float f -> {
                return AttributeValue.fromB(SdkBytes.fromByteArray(DoubleSortOrder.toSortableBytes(f)));
            }
            case Double d -> {
                return AttributeValue.fromB(SdkBytes.fromByteArray(DoubleSortOrder.toSortableBytes(d)));
            }
            case null, default -> throw new IllegalStateException("Object type is unfit to be a sort key");
        }
    }

    private static AttributeValue toTtl(Object object){
        var epoch = toEpochTtl(object);
        return AttributeValue.fromN(String.valueOf(epoch));
    }

    @SneakyThrows
    private static <T> Map<String, AttributeValue> serialize(Set<Object> walked, T entity, Class<T> klass){
        if (!walked.add(entity)){
            throw new IllegalStateException("Cyclic reference detected");
        }
        var map = new HashMap<String, AttributeValue>();
        map.put("$class", AttributeValue.fromS(entity.getClass().getName()));

        var methodNames = Arrays.stream(klass.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        for (var field : klass.getDeclaredFields()){
            if (!isPublicGetter(field.getName(), field.getType(), methodNames)){
                continue;
            }
            field.setAccessible(true);

            var value = field.get(entity);
            if (value == null){
                map.put(field.getName(), AttributeValue.fromNul(true));
                continue;
            } else if (field.isAnnotationPresent(PK.class)){
                if (map.put("pk", toAttribute(walked, value)) != null){
                    throw new IllegalStateException("Duplicated PK");
                }

                continue;
            } else if (field.isAnnotationPresent(SK.class)){
                if (map.put("sk", toSk(value)) != null){
                    throw new IllegalStateException("Duplicated SK");
                }
            } else if (field.isAnnotationPresent(TTL.class)) {
                if (map.put("__ttl", toTtl(value)) != null){
                    throw new IllegalStateException("Duplicated TTL");
                }
            }
            if (map.put(field.getName(), toAttribute(walked, value)) != null){
                throw new IllegalStateException("Duplicated field: " + field.getName());
            }
        }

        for (var method : klass.getDeclaredMethods()){
            if (Modifier.isStatic(method.getModifiers())){
                continue;
            }
            if (method.getParameterCount() > 0){
                continue;
            }
            if (method.getName().length() < 4 || !method.getName().startsWith("get")){
                continue;
            }

            if (method.isAnnotationPresent(PK.class)){
                var value = method.invoke(entity);
                if (map.put("pk", toAttribute(walked, value)) != null){
                    throw new IllegalStateException("Duplicated PK");
                }
            } else if (method.isAnnotationPresent(SK.class)){
                var value = method.invoke(entity);
                if (map.put("sk", toSk(value)) != null){
                    throw new IllegalStateException("Duplicated SK");
                }

                var propName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().length() >= 5 ? method.getName().substring(4) : "";
                if (map.put(propName, toAttribute(walked, value)) != null){
                    throw new IllegalStateException("Duplicated field: " + propName);
                }
            }
        }

        return map;
    }

    public static <T> Map<String, AttributeValue> serialize(T entity, Class<T> klass){
        Objects.requireNonNull(entity);
        return serialize(new HashSet<>(), entity, klass);
    }

    @SneakyThrows
    private static Object deserializeCollectionAttribute(Map<String, AttributeValue> map){
        if (map == null){
            return null;
        }
        var klass = map.get("$class");
        var value = map.get("$value");
        if (value != null){
            return deserialize(value, Class.forName(klass.s()));
        }

        return deserialize(map);
    }

    private static Object deserialize(AttributeValue attributeValue, Class<?> klass){
        if (Boolean.TRUE.equals(attributeValue.nul())){
            return null;
        }
        if (String.class.equals(klass)){
            return attributeValue.s();
        }
        if (OffsetDateTime.class.equals(klass)){
            return OffsetDateTime.parse(attributeValue.s());
        }
        if (boolean.class.equals(klass) || Boolean.class.equals(klass)){
            return attributeValue.bool();
        }
        if (int.class.equals(klass) || Integer.class.equals(klass)){
            return Integer.parseInt(attributeValue.n());
        }
        if (long.class.equals(klass) || Long.class.equals(klass)){
            return Long.parseLong(attributeValue.n());
        }
        if (float.class.equals(klass) || Float.class.equals(klass)){
            return Float.parseFloat(attributeValue.n());
        }
        if (double.class.equals(klass) || Double.class.equals(klass)){
            return Double.parseDouble(attributeValue.n());
        }
        if (Collection.class.isAssignableFrom(klass)){
            return attributeValue.l().stream()
                    .map(a -> {
                        if (Boolean.TRUE.equals(a.nul())){
                            return null;
                        }

                        return a.m();
                    })
                    .map(DDBCrudRepository::deserializeCollectionAttribute)
                    .toList();
        }
        return deserialize(attributeValue.m());
    }

    @SneakyThrows
    private static <T> T deserialize(Map<String, AttributeValue> map){
        var klass = Class.forName(map.get("$class").s());
        var entity = klass.getDeclaredConstructor().newInstance();
        var methodNames = Arrays.stream(klass.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
        for (var field : klass.getDeclaredFields()){
            if (!isPublicSetter(field.getName(), field.getType(), methodNames)){
                continue;
            }
            field.setAccessible(true);

            String name;
            if (field.isAnnotationPresent(PK.class)){
                name = "pk";
            } else {
                name = field.getName();
            }
            var attr = map.get(name);
            if (attr == null){
                continue;
            }
            field.set(entity, deserialize(attr, field.getType()));
        }

        return (T) entity;
    }

    public static <T> T deserialize(Map<String, AttributeValue> map, Class<T> klass){
        Objects.requireNonNull(map);
        var entity = deserialize(map);
        if (!klass.isAssignableFrom(entity.getClass())){
            throw new IllegalStateException("Failed to deserialize entity");
        }

        return (T)entity;
    }

    @Override
    public void save(T entity) {
        var map = serialize(entity, (Class<? super T>) entity.getClass());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(map)
                .build());
    }

    @Override
    public void create(T entity) {
        var map = serialize(entity, (Class<? super T>) entity.getClass());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(map)
                .conditionExpression("attribute_not_exists(sk)")
                .build());
    }

    @Override
    public Optional<T> find(TPK pk, TSK sk) {
        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("pk", toAttribute(new HashSet<>(), pk),
                        "sk", toSk(sk)))
                .build());
        if (!response.hasItem()){
            return Optional.empty();
        }

        var map = response.item();
        return Optional.of(deserialize(map));
    }

    @Override
    public void delete(TPK pk, TSK sk) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("pk", toAttribute(new HashSet<>(), pk),
                        "sk", toSk(sk)))
                .build());
    }
}