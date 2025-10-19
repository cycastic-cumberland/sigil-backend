package net.cycastic.sigil.configuration.aws.ssm;

import lombok.NonNull;
import net.cycastic.sigil.configuration.aws.AWSConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class AWSSSMEngineConfigDataLocationResolver extends AWSConfigDataLocationResolver<AWSSSMEngineConfigDataResource> {
    @Override
    protected String getPrefix() {
        return "aws-ssm-params:";
    }

    @Override
    protected List<AWSSSMEngineConfigDataResource> resolveInternal(@NonNull ConfigDataLocationResolverContext context, ConfigDataLocation location, String path, Region region) {
        return List.of(AWSSSMEngineConfigDataResource.builder()
                .path(path)
                .region(region)
                .build());
    }

    @Override
    public int getOrder() {
        return super.getOrder() + 1;
    }
}
