package net.cycastic.sigil.service.impl.job;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.job.BackgroundJobDetails;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import net.cycastic.sigil.service.job.JobActivator;
import net.cycastic.sigil.service.job.JobScheduler;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LocalJobActivator implements JobActivator, JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(LocalJobActivator.class);

    private final ConcurrentHashMap<Class, Supplier<BackgroundJobHandler>> handlerMap = new ConcurrentHashMap<>();
    private final ObjectProvider<BackgroundJobHandler> validators;
    private final JsonSerializer jsonSerializer;
    private final TaskExecutor taskExecutor;

    private <T> Supplier<BackgroundJobHandler<T>> getHandlerTyped(Class<T> klass){
        for (var handler : validators){
            if (!handler.matches(klass)){
                continue;
            }

            return () -> handler;
        }

        return () -> {
            throw new RequestException(500, "Handler not found for job type " + klass.getName());
        };
    }

    private Supplier<BackgroundJobHandler> getHandler(Class klass){
        return getHandlerTyped(klass);
    }

    @SneakyThrows
    public void process(BackgroundJobDetails job){
        var klass = Class.forName(job.getRequestClass());
        var handler = handlerMap.computeIfAbsent(klass, this::getHandler).get();

        var deserializedData = jsonSerializer.deserialize(job.getData(), klass);

        logger.debug("Processing job {}", klass.getName());
        handler.process(deserializedData);
    }

    /**
     * Locally dispatch task without retry or backoff.
     * @param data Data payload.
     * @param klass Data payload's class.
     * @param <T> Data payload type parameter.
     * @param <C> Data payload's class type parameter.
     */
    @Override
    public <T extends C, C> void defer(T data, Class<C> klass) {
        var handler = getHandlerTyped(klass).get();
        taskExecutor.execute(() -> handler.process(data));
    }
}
