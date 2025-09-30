package net.cycastic.sigil.service.impl.job.sqs;

import net.cycastic.sigil.configuration.job.SqsJobQueueConfigurations;
import net.cycastic.sigil.service.job.JobActivator;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Optional;

@Component
public class ConfigurableSqsJobDispatcher implements SmartLifecycle, AutoCloseable {
    private final SqsJobDispatcher sqsJobDispatcher;

    public ConfigurableSqsJobDispatcher(ApplicationContext applicationContext,
                                        Optional<SqsJobQueueConfigurations> sqsJobQueueConfigurations){
        if (sqsJobQueueConfigurations.isEmpty() || sqsJobQueueConfigurations.get().getQueueUrl() == null){
            sqsJobDispatcher = null;
            return;
        }

        sqsJobDispatcher = new SqsJobDispatcher(applicationContext.getBean(TaskExecutor.class),
                applicationContext.getBean(JsonSerializer.class),
                applicationContext.getBean(JobActivator.class),
                sqsJobQueueConfigurations.get(),
                applicationContext.getBean(AwsCredentialsProvider.class));
    }

    @Override
    public void start() {
        if (sqsJobDispatcher != null){
            sqsJobDispatcher.start();
        }
    }

    @Override
    public void stop() {
        if (sqsJobDispatcher != null){
            sqsJobDispatcher.stop();
        }
    }

    @Override
    public boolean isRunning() {
        if (sqsJobDispatcher != null){
            return sqsJobDispatcher.isRunning();
        }

        return false;
    }

    @Override
    public void close() {
        if (sqsJobDispatcher != null){
            sqsJobDispatcher.close();
        }
    }
}
