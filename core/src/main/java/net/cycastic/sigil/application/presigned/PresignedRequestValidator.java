package net.cycastic.sigil.application.presigned;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.impl.UriPresigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class PresignedRequestValidator implements CommandValidator {
    private static final Logger logger = LoggerFactory.getLogger(PresignedRequestValidator.class);
    private final UriPresigner uriPresigner;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public boolean matches(Class klass) {
        return PresignedRequest.class.isAssignableFrom(klass);
    }

    @Override
    public void validate(Command command) {
        var request = (PresignedRequest)command;

        var url = loggedUserAccessor.getRequestPath();
        if (!uriPresigner.verifyUri(URI.create(url))){
            logger.error("Request signature validation failed for URL {}", url);
            throw RequestException.withExceptionCode("C403T002");
        }

        var now = OffsetDateTime.now();
        var nvb = OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.getNotValidBefore()), ZoneOffset.UTC);
        var nva = OffsetDateTime.ofInstant(Instant.ofEpochSecond(request.getNotValidAfter()), ZoneOffset.UTC);
        if (now.isBefore(nvb)){
            throw RequestException.withExceptionCode("C403T004");
        }

        if (now.isAfter(nva)){
            throw RequestException.withExceptionCode("C403T004");
        }
    }
}
