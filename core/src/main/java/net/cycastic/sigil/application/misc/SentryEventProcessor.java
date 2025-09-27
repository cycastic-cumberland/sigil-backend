package net.cycastic.sigil.application.misc;

import io.sentry.Hint;
import io.sentry.SentryBaseEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.SentryTransaction;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SentryEventProcessor implements SentryOptions.BeforeSendTransactionCallback {
    private <T extends SentryBaseEvent> T executeInternal(@NonNull T event) {
        if (event.getRequest() != null && event.getRequest().getUrl() != null) {
            var uri = URI.create(event.getRequest().getUrl());
            if (uri.getPath().startsWith("/actuator/")) {
                return null;
            }
        }
        return event;
    }

    @Override
    public SentryTransaction execute(@NonNull SentryTransaction event, @NonNull Hint hint) {
        return executeInternal(event);
    }
}
