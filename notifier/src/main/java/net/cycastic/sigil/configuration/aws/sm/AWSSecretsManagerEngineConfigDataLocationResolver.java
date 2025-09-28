package net.cycastic.sigil.configuration.aws.sm;

import lombok.NonNull;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.core.Ordered;
import software.amazon.awssdk.regions.Region;

import java.util.Collections;
import java.util.List;

public class AWSSecretsManagerEngineConfigDataLocationResolver implements ConfigDataLocationResolver<@NonNull AWSSecretsManagerEngineConfigDataResource>, Ordered {

    private static final String PREFIX = "aws-sm:";

    @Override
    public boolean isResolvable(@NonNull ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        return location.hasPrefix(PREFIX);
    }

    @Override
    public @NonNull List<AWSSecretsManagerEngineConfigDataResource> resolve(@NonNull ConfigDataLocationResolverContext context,
                                                                            ConfigDataLocation location)
            throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {

        var path = location.getNonPrefixedValue(PREFIX);
        var firstSlash = path.indexOf('/');
        if (firstSlash < 0){
            throw new ConfigDataLocationNotFoundException(location);
        }
        return Collections.singletonList(AWSSecretsManagerEngineConfigDataResource.builder()
                        .region(Region.of(path.substring(0, firstSlash)))
                        .path(path.substring(firstSlash))
                .build());
    }

    @Override
    public @NonNull List<AWSSecretsManagerEngineConfigDataResource> resolveProfileSpecific(
            @NonNull ConfigDataLocationResolverContext context,
            @NonNull ConfigDataLocation location,
            @NonNull Profiles profiles)
            throws ConfigDataLocationNotFoundException {
        return Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}