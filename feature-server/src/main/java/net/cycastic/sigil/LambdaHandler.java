package net.cycastic.sigil;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Supplier;

public class LambdaHandler implements RequestStreamHandler {
    @RequiredArgsConstructor
    private static class Lazy<T> {
        private final Supplier<T> supplier;
        private Optional<T> result = Optional.empty();

        public synchronized T get(){
            if (result.isEmpty()){
                var value = supplier.get();
                result = Optional.of(value);
                return value;
            }

            return result.get();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(LambdaHandler.class);
    private static final Lazy<SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse>> handler = new Lazy<>(() -> {
        try {
            return SpringBootLambdaContainerHandler.getAwsProxyHandler(FeatureServerApplication.class);
            // If you are using HTTP APIs with the version 2.0 of the proxy model, use the getHttpApiV2ProxyHandler
            // method: handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            logger.error("Failed to initialize lambda container", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    });

    /**
     * It loads the class that's all lol.
     */
    public static void loadClass(){}

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.get().proxyStream(inputStream, outputStream, context);
    }
}
