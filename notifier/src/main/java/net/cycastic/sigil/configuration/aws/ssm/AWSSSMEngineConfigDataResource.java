package net.cycastic.sigil.configuration.aws.ssm;

import lombok.*;
import org.springframework.boot.context.config.ConfigDataResource;
import software.amazon.awssdk.regions.Region;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AWSSSMEngineConfigDataResource extends ConfigDataResource {
    private Region region;
    private String path;
}
