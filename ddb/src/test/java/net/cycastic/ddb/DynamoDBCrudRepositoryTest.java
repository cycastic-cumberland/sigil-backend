package net.cycastic.ddb;

import lombok.*;
import net.cycastic.ddb.annotation.PK;
import net.cycastic.ddb.annotation.SK;
import net.cycastic.ddb.annotation.TTL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
public class DynamoDBCrudRepositoryTest {
    private record Id(AttributeValue pk, AttributeValue sk){}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataClass {
        private String name;

        private String description;

        private float value;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockEntity{
        private String id;

        private int tenantId;

        private String type;

        private int subType;

        @TTL
        private OffsetDateTime expiration;

        private List<DataClass> data;

        private Collection<?> any;

        @EqualsAndHashCode.Exclude
        private Object cyclic;

        private boolean isTrue;

        private boolean isFalse;

        @PK
        public String getEntitlement(){
            return id + '#' + tenantId;
        }

        @SK
        public String getSubType(){
           return type + '#' + subType;
        }

    }

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Test
    public void testSerialization(){
        var any = new ArrayList<>();
        any.add(12);
        any.add(null);
        any.add(9.2);
        any.add("Hello");
        any.add(OffsetDateTime.now());

        var originalData = MockEntity.builder()
                .id("test")
                .tenantId(6969)
                .type("aa")
                .subType(12)
                .expiration(OffsetDateTime.now())
                .data(List.of(DataClass.builder()
                        .name("n")
                        .description("a")
                        .value(69.6969f)
                        .build()))
                .any(any)
                .build();
        var serialized = DDBCrudRepository.serialize(originalData, MockEntity.class);
        var deserialized = DDBCrudRepository.deserialize(serialized, MockEntity.class);
        assertNotSame(originalData, deserialized);
        assertEquals(originalData, deserialized);
    }

    @Test
    public void testCyclicReference(){
        var repository = new DDBCrudRepository<MockEntity, String, String>(dynamoDbClient, "test");
        var originalData = MockEntity.builder()
                .id("test")
                .tenantId(6969)
                .expiration(OffsetDateTime.now())
                .data(List.of(DataClass.builder()
                        .name("n")
                        .description("a")
                        .value(69.6969f)
                        .build()))
                .isTrue(false)
                .isFalse(true)
                .build();
        originalData.cyclic = List.of(List.of(List.of(originalData)));
        assertThrows(IllegalStateException.class, () -> repository.save(originalData));
    }

    @Test
    public void testDdbApi(){
        var repository = new DDBCrudRepository<MockEntity, String, String>(dynamoDbClient, "test");
        final var store = new HashMap<Id, Map<String, AttributeValue>>();
        Mockito.doAnswer(a -> {
            PutItemRequest request = a.getArgument(0);
            store.put(new Id(request.item().get("pk"), request.item().get("sk")), request.item());
            return PutItemResponse.builder()
                    .build();
        })
                .when(dynamoDbClient)
                .putItem(Mockito.any(PutItemRequest.class));
        Mockito.doAnswer(a -> {
            GetItemRequest request = a.getArgument(0);
            var item = store.get(new Id(request.key().get("pk"), request.key().get("sk")));
            return GetItemResponse.builder()
                    .item(item)
                    .build();
        })
                .when(dynamoDbClient)
                .getItem(Mockito.any(GetItemRequest.class));

        var firstEntity = MockEntity.builder()
                .id("test")
                .tenantId(6969)
                .type("aa")
                .subType(12)
                .build();
        repository.save(firstEntity);
        assertEquals(1, store.size());
        var secondEntity = MockEntity.builder()
                .id("test")
                .tenantId(6969)
                .type("aa")
                .subType(12)
                .expiration(OffsetDateTime.now())
                .build();
        repository.save(secondEntity);
        assertEquals(1, store.size());

        var nonOp = repository.find("test#6969", "aa#11");
        assertTrue(nonOp.isEmpty());

        var secondEntityRepOp = repository.find("test#6969", "aa#12");
        assertTrue(secondEntityRepOp.isPresent());
        var secondEntityRep = secondEntityRepOp.get();
        assertNotSame(firstEntity, secondEntityRep);
        assertNotSame(secondEntity, secondEntityRep);
        assertNotEquals(firstEntity, secondEntityRep);
        assertEquals(secondEntity, secondEntityRep);
    }
}
