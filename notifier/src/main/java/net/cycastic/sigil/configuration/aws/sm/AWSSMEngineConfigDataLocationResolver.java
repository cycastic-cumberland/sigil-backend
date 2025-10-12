package net.cycastic.sigil.configuration.aws.sm;

import lombok.NonNull;
import net.cycastic.sigil.configuration.aws.AWSConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class AWSSMEngineConfigDataLocationResolver extends AWSConfigDataLocationResolver<AWSSMEngineConfigDataResource> {
    @Override
    protected String getPrefix() {
        return "aws-sm:";
    }

    @Override
    protected List<AWSSMEngineConfigDataResource> resolveInternal(@NonNull ConfigDataLocationResolverContext context, ConfigDataLocation location, String path, Region region) {
        return List.of(AWSSMEngineConfigDataResource.builder()
                        .path(path)
                        .region(region)
                .build());
    }
}