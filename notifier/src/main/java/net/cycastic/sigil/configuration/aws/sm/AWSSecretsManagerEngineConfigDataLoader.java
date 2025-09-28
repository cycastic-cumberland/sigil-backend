package net.cycastic.sigil.configuration.aws.sm;

import lombok.NonNull;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class AWSSecretsManagerEngineConfigDataLoader implements ConfigDataLoader<@NonNull AWSSecretsManagerEngineConfigDataResource> {
    @Override
    public ConfigData load(@NonNull ConfigDataLoaderContext context, AWSSecretsManagerEngineConfigDataResource resource)
            throws ConfigDataResourceNotFoundException {
        try {
            var props = loadPropertiesFromCustomSource(resource.getPath(), resource.getRegion());
            var propertySource = new MapPropertySource("aws-sm", props);
            return new ConfigData(List.of(propertySource));

        } catch (Exception ex) {
            throw new ConfigDataResourceNotFoundException(resource, ex);
        }
    }

    private Map<String, Object> loadPropertiesFromCustomSource(String path, Region region) {
        try (var client = SecretsManagerClient.builder().region(region).build()) {
            var request = GetSecretValueRequest.builder()
                    .secretId(path)
                    .build();
            var response = client.getSecretValue(request);
            var secretString = response.secretString();

            var mapper = new ObjectMapper();
            return mapper.readValue(secretString, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load secret from AWS Secrets Manager", e);
        }
    }
}
