package net.cycastic.sigil.configuration.job;

import net.cycastic.sigil.service.impl.job.LocalJobActivator;
import net.cycastic.sigil.service.impl.job.sqs.SqsJobScheduler;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import net.cycastic.sigil.service.job.JobScheduler;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@Component
public class SchedulingConfigurations {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfigurations.class);

    @Lazy
    @Bean
    public static SqsJobScheduler sqsJobScheduler(JsonSerializer jsonSerializer,
                                                  SqsJobQueueConfigurations sqsJobQueueConfigurations,
                                                  AwsCredentialsProvider awsCredentialsProvider) {
        return new SqsJobScheduler(jsonSerializer, sqsJobQueueConfigurations, awsCredentialsProvider);
    }

    @Lazy
    @Bean
    public static LocalJobActivator localJobActivator(ObjectProvider<BackgroundJobHandler> validators, JsonSerializer jsonSerializer, TaskExecutor taskScheduler){
        return new LocalJobActivator(validators, jsonSerializer, taskScheduler);
    }

    @Bean
    public JobScheduler jobScheduler(ApplicationContext applicationContext,
                                            SqsJobQueueConfigurations sqsJobQueueConfigurations){
        JobScheduler scheduler;
        if (sqsJobQueueConfigurations.getQueueUrl() == null){
            scheduler = applicationContext.getBean(LocalJobActivator.class);
        } else {
            scheduler = applicationContext.getBean(SqsJobScheduler.class);
        }

        logger.info("Job scheduler: {}", scheduler.getClass().getName());
        return scheduler;
    }
}
