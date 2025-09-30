package net.cycastic.sigil.configuration.job;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.configuration.aws.BaseSqsConfigurations;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "application.job.sqs")
public class SqsJobQueueConfigurations extends BaseSqsConfigurations {
    private Integer waitTimeSeconds;
    private Integer maxNumberOfMessages;
}
