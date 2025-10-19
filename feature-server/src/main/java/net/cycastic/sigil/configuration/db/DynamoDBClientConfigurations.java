package net.cycastic.sigil.configuration.db;

import software.amazon.awssdk.regions.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDBClientConfigurations {
    @Bean
    public DynamoDbClient dynamoDbClient(DynamoDBConfigurations dynamoDBConfigurations){
        return DynamoDbClient.builder()
                .region(Region.of(dynamoDBConfigurations.getRegion()))
                .build();
    }
}
