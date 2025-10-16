package net.cycastic.sigil.service.impl.job.sqs;

import net.cycastic.sigil.configuration.job.SqsJobQueueConfigurations;
import net.cycastic.sigil.service.CorrelationIdProvider;
import net.cycastic.sigil.service.job.BackgroundJob;
import net.cycastic.sigil.service.job.BackgroundJobDetails;
import net.cycastic.sigil.service.job.JobDetails;
import net.cycastic.sigil.service.job.JobScheduler;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SqsJobScheduler implements JobScheduler, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SqsJobScheduler.class);
    private final JsonSerializer jsonSerializer;
    private final SqsClient sqsClient;
    private final String queueUrl;
    private final CorrelationIdProvider correlationIdProvider;

    public SqsJobScheduler(JsonSerializer jsonSerializer,
                           SqsJobQueueConfigurations sqsJobQueueConfigurations,
                           AwsCredentialsProvider awsCredentialsProvider,
                           CorrelationIdProvider correlationIdProvider){
        this.jsonSerializer = jsonSerializer;
        this.correlationIdProvider = correlationIdProvider;
        sqsClient = SqsClient.builder()
                .region(Region.of(sqsJobQueueConfigurations.getRegionName()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
        queueUrl = sqsJobQueueConfigurations.getQueueUrl();
    }

    private void queueItem(JobDetails jobDetails){
        var serialized = jsonSerializer.serialize(jobDetails);
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(serialized)
                .build());
        logger.debug("Scheduled item {}", jobDetails.getRequestClass());
    }

    @Override
    public <T extends C, C extends BackgroundJob> void defer(T data, Class<C> klass) {
        queueItem(BackgroundJobDetails.builder()
                .id(UUID.randomUUID())
                .correlationId(correlationIdProvider.getCorrelationId())
                .scheduledAt(OffsetDateTime.now())
                .requestClass(klass.getName())
                .data(jsonSerializer.serialize(data))
                .build());
    }

    @Override
    public <T extends C, C extends BackgroundJob> void deferInfallible(T data, Class<C> klass) {
        try {
            defer(data, klass);
        } catch (Exception e){
            logger.error("Failed to dispatch job {}", klass.getName(), e);
        }
    }

    @Override
    public void close() {
        sqsClient.close();
    }
}
