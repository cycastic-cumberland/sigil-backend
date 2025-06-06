package net.cycastic.portfoliotoolkit.configuration;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Notification;
import an.awesome.pipelinr.Pipelinr;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipelinrConfiguration {
    @Bean
    public Pipelinr pipelinr(ApplicationContext context) {
        return new Pipelinr()
                .with(() -> context.getBeansOfType(Command.Handler.class).values().stream())
                .with(() -> context.getBeansOfType(Notification.Handler.class).values().stream())
                .with(() -> context.getBeansOfType(Notification.Middleware.class).values().stream())
                .with(() -> context.getBeansOfType(Command.Middleware.class).values().stream());
    }
}
