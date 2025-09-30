package net.cycastic.sigil.configuration.mail;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.configuration.aws.BaseSqsConfigurations;
import net.cycastic.sigil.configuration.storage.BaseS3Configurations;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqsRemoteEmailConfigurations extends BaseSqsConfigurations {
    private BaseS3Configurations bucket;
}
