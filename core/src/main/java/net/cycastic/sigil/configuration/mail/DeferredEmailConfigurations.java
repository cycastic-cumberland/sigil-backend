package net.cycastic.sigil.configuration.mail;

import net.cycastic.sigil.service.email.DeferredEmailSender;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import net.cycastic.sigil.service.impl.email.LocalDeferredEmailSender;
import net.cycastic.sigil.service.impl.email.SqsRemoteEmailSender;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class DeferredEmailConfigurations {
    @Bean
    public DeferredEmailSender deferredEmailSender(ApplicationContext ctx,
                                                   ApplicationEmailConfigurations applicationEmailConfigurations){
        if (applicationEmailConfigurations.getSqs() != null){
            return new SqsRemoteEmailSender(applicationEmailConfigurations.getSender(),
                    ctx.getBean(JsonSerializer.class),
                    applicationEmailConfigurations.getSqs());
        }

        return new LocalDeferredEmailSender(ctx.getBean(TaskExecutor.class), ctx.getBean(ApplicationEmailSender.class));
    }
}
