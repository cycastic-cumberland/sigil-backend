package net.cycastic.sigil.configuration.aws.ssm;

import lombok.NonNull;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSSSMEngineConfigDataLoader implements ConfigDataLoader<@NonNull AWSSSMEngineConfigDataResource>  {
    @Override
    public ConfigData load(@NonNull ConfigDataLoaderContext context, AWSSSMEngineConfigDataResource resource)
            throws ConfigDataResourceNotFoundException {
        try {
            var props = loadPropertiesFromCustomSource(resource.getPath(), resource.getRegion());
            var propertySource = new MapPropertySource("aws-ssm-params", props);
            return new ConfigData(List.of(propertySource));

        } catch (RuntimeException ex){
            throw ex;
        } catch (Exception ex) {
            throw new ConfigDataResourceNotFoundException(resource, ex);
        }
    }

    private Map<String, Object> loadPropertiesFromCustomSource(String path, Region region) {
        path = path.endsWith("/") ? path : path + "/";
        try (var client = SsmClient.builder().region(region).build())  {
            var map = new HashMap<String, Object>();
            String nextToken = null;

            do {
                var request = GetParametersByPathRequest.builder()
                        .path(path)
                        .recursive(true)
                        .withDecryption(true)
                        .nextToken(nextToken)
                        .build();

                var response = client.getParametersByPath(request);

                for (var param : response.parameters()) {
                    var key = param.name().substring(path.length())
                            .replace('/', '.');
                    if (key.isEmpty()){
                        continue;
                    }
                    map.put(key, param.value());
                }

                nextToken = response.nextToken();
            } while (nextToken != null);

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load secret from AWS Secrets Manager", e);
        }
    }
}
