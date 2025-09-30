package net.cycastic.sigil.configuration.aws;

import lombok.Data;

@Data
public class BaseSqsConfigurations {
    private String regionName;
    private String queueUrl;
}
