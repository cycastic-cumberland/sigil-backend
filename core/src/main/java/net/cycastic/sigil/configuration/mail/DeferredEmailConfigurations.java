package net.cycastic.sigil.configuration.mail;

import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.email.DeferredEmailSender;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import net.cycastic.sigil.service.impl.email.LocalDeferredEmailSender;
import net.cycastic.sigil.service.impl.email.SqsRemoteEmailSender;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@Configuration
public class DeferredEmailConfigurations {
    private static final Logger logger = LoggerFactory.getLogger(DeferredEmailConfigurations.class);

    @Bean
    @Lazy
    public static ApplicationEmailSender applicationEmailSender(ApplicationEmailConfigurations configurations, DecryptionProvider decryptionProvider){
        return new ApplicationEmailSender(configurations, decryptionProvider);
    }

    @Bean
    public DeferredEmailSender deferredEmailSender(ApplicationContext ctx,
                                                   ApplicationEmailConfigurations applicationEmailConfigurations){
        DeferredEmailSender deferredEmailSender;
        if (applicationEmailConfigurations.getSqs() != null){
            deferredEmailSender = new SqsRemoteEmailSender(applicationEmailConfigurations.getSender(),
                    ctx.getBean(JsonSerializer.class),
                    applicationEmailConfigurations.getSqs(),
                    ctx.getBean(AwsCredentialsProvider.class));
        } else {
            deferredEmailSender = new LocalDeferredEmailSender(ctx.getBean(TaskExecutor.class), ctx.getBean(ApplicationEmailSender.class));
        }

        logger.info("Deferred email sender: {}", deferredEmailSender.getClass().getName());
        return deferredEmailSender;
    }
}
