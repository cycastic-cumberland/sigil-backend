package net.cycastic.sigil.service.impl.job.sqs;

import net.cycastic.sigil.configuration.job.SqsJobQueueConfigurations;
import net.cycastic.sigil.service.job.BackgroundJobDetails;
import net.cycastic.sigil.service.job.JobDetails;
import net.cycastic.sigil.service.job.JobScheduler;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SqsJobScheduler implements JobScheduler, AutoCloseable {
    private final JsonSerializer jsonSerializer;
    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsJobScheduler(JsonSerializer jsonSerializer,
                            SqsJobQueueConfigurations sqsJobQueueConfigurations,
                            AwsCredentialsProvider awsCredentialsProvider){
        this.jsonSerializer = jsonSerializer;
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
    }

    @Override
    public <T extends C, C> void defer(T data, Class<C> klass) {
        queueItem(BackgroundJobDetails.builder()
                .id(UUID.randomUUID())
                .scheduledAt(OffsetDateTime.now())
                .requestClass(klass.getName())
                .data(jsonSerializer.serialize(data))
                .build());
    }

    @Override
    public void close() {
        sqsClient.close();
    }
}
