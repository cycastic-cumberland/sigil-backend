package net.cycastic.sigil.configuration.aws;

import lombok.NonNull;
import org.springframework.boot.context.config.*;
import org.springframework.core.Ordered;
import software.amazon.awssdk.regions.Region;

import java.util.Collections;
import java.util.List;

public abstract class AWSConfigDataLocationResolver<R extends ConfigDataResource> implements ConfigDataLocationResolver<@NonNull R>, Ordered {
    protected abstract String getPrefix();

    protected abstract List<R> resolveInternal(@NonNull ConfigDataLocationResolverContext context,
                                               ConfigDataLocation location,
                                               String path,
                                               Region region);

    public int getOrder(){
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean isResolvable(@NonNull ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        return location.hasPrefix(getPrefix());
    }

    @Override
    public @NonNull List<R> resolve(@NonNull ConfigDataLocationResolverContext context,
                                                        ConfigDataLocation location)
            throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {

        var path = location.getNonPrefixedValue(getPrefix());
        var firstSlash = path.indexOf('/');
        if (firstSlash < 0){
            throw new ConfigDataLocationNotFoundException(location);
        }
        return resolveInternal(context, location, path.substring(firstSlash), Region.of(path.substring(0, firstSlash)));
    }

    @Override
    public @NonNull List<R> resolveProfileSpecific(
            @NonNull ConfigDataLocationResolverContext context,
            @NonNull ConfigDataLocation location,
            @NonNull Profiles profiles)
            throws ConfigDataLocationNotFoundException {
        return Collections.emptyList();
    }
}
