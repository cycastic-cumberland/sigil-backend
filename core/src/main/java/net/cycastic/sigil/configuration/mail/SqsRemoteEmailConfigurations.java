package net.cycastic.sigil.configuration.mail;

import lombok.Data;
import net.cycastic.sigil.configuration.storage.BaseS3Configurations;

@Data
public class SqsRemoteEmailConfigurations {
    private String regionName;
    private String queueUrl;
    private BaseS3Configurations bucket;
}
