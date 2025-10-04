package net.cycastic.sigil.service.impl.job.sqs;

import lombok.Getter;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.job.SqsJobQueueConfigurations;
import net.cycastic.sigil.service.job.BackgroundJobDetails;
import net.cycastic.sigil.service.job.JobActivator;
import net.cycastic.sigil.service.job.JobDetailsDto;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SqsJobDispatcher implements SmartLifecycle, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SqsJobDispatcher.class);

    private record SuccessHandle(String id, String receiptHandle){}

    private static class SqsRoutineContext implements AutoCloseable {

        private final SqsJobDispatcher dispatcher;
        private final Collection<SuccessHandle> successHandles;

        @Getter
        private final List<Message> messages;

        public SqsRoutineContext(SqsJobDispatcher dispatcher, List<Message> messages){
            this.dispatcher = dispatcher;
            this.messages = messages;
            successHandles = HashSet.newHashSet(messages.size());
        }

        public void complete(Message message){
            successHandles.add(new SuccessHandle(message.messageId(), message.receiptHandle()));
        }

        @Override
        public void close() {
            if (successHandles.isEmpty()){
                return;
            }

            logger.debug("Marking {} job(s) as completed", successHandles.size());
            dispatcher.sqsClient.deleteMessageBatch(d -> d.queueUrl(dispatcher.sqsJobQueueConfigurations.getQueueUrl())
                    .entries(successHandles.stream()
                            .map(h -> DeleteMessageBatchRequestEntry.builder()
                                    .id(h.id)
                                    .receiptHandle(h.receiptHandle)
                                    .build())
                            .toList()));
        }
    }

    private final TaskExecutor taskScheduler;
    private final JsonSerializer jsonSerializer;
    private final JobActivator jobActivator;
    private final SqsClient sqsClient;
    private final SqsJobQueueConfigurations sqsJobQueueConfigurations;
    private boolean started;
    private boolean stopped;

    public SqsJobDispatcher(TaskExecutor taskScheduler,
                            JsonSerializer jsonSerializer,
                            JobActivator jobActivator,
                            SqsJobQueueConfigurations sqsJobQueueConfigurations,
                            AwsCredentialsProvider awsCredentialsProvider) {
        this.taskScheduler = taskScheduler;
        this.jsonSerializer = jsonSerializer;
        this.jobActivator = jobActivator;
        this.sqsJobQueueConfigurations = sqsJobQueueConfigurations;

        sqsClient = SqsClient.builder()
                .region(Region.of(sqsJobQueueConfigurations.getRegionName()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    public void start() {
        taskScheduler.execute(this::startPollLoop);
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    private void poll(){
        var response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(sqsJobQueueConfigurations.getQueueUrl())
                .waitTimeSeconds(sqsJobQueueConfigurations.getWaitTimeSeconds() == null || sqsJobQueueConfigurations.getWaitTimeSeconds() < 1
                        ? 5
                        : sqsJobQueueConfigurations.getWaitTimeSeconds())
                .maxNumberOfMessages(sqsJobQueueConfigurations.getMaxNumberOfMessages() == null || sqsJobQueueConfigurations.getMaxNumberOfMessages() < 1
                        ? 10
                        : sqsJobQueueConfigurations.getMaxNumberOfMessages())
                .build());

        if (!response.hasMessages()){
            return;
        }

        logger.info("Processing batch job of {} item(s)", response.messages().size());
        try (var context = new SqsRoutineContext(this, response.messages())){
            for (var record : context.getMessages()){
                try {
                    var typeDto = jsonSerializer.deserialize(record.body(), JobDetailsDto.class);
                    var completed = true;
                    switch (typeDto.getJobType()){
                        case DEFERRED -> jobActivator.process(jsonSerializer.deserialize(record.body(), BackgroundJobDetails.class));
                        default -> {
                            logger.error("Unexpected job type of message ID {}: {}", record.messageId(), typeDto.getJobType());
                            completed = false;
                        }
                    }

                    if (completed){
                        logger.debug("Job completed: {}", record.messageId());
                        context.complete(record);
                    }
                } catch (Exception e){
                    logger.error("Failed to process job with ID {}", record.messageId(), e);
                }
            }
        }
    }

    @SneakyThrows
    private void startPollLoop(){
        started = true;
        logger.info("SQS Job dispatcher ready");

        while (!stopped) {
            try {
                poll();
            } catch (Exception e) {
                logger.error("Exception caught in main poll routine", e);
                //noinspection BusyWait
                Thread.sleep(3000);
            }
        }

        logger.info("SQS Job dispatcher stopped");
        started = false;
    }

    @Override
    public void close() {
        sqsClient.close();
    }
}
