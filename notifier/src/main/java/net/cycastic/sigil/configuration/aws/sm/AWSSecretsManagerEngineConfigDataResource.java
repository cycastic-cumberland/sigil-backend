package net.cycastic.sigil.configuration.aws.sm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.config.ConfigDataResource;
import software.amazon.awssdk.regions.Region;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AWSSecretsManagerEngineConfigDataResource extends ConfigDataResource {
    private Region region;
    private String path;
}