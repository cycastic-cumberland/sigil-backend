package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.UserStatus;
import net.cycastic.sigil.domain.repository.UserRepository;
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
public class CompleteUserRegistrationCommandHandler implements Command.Handler<CompleteUserRegistrationCommand, @Null Object> {
    private static final Logger logger = LoggerFactory.getLogger(CompleteUserRegistrationCommandHandler.class);
    private final UriPresigner uriPresigner;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public @Null Object handle(CompleteUserRegistrationCommand command) {
        var url = loggedUserAccessor.getRequestPath();
        if (!uriPresigner.verifyUri(URI.create(url))){
            throw new RequestException(401, "Signature verification failed");
        }

        var now = OffsetDateTime.now();
        if (now.isBefore(OffsetDateTime.ofInstant(Instant.ofEpochSecond(command.getNotValidBefore()), ZoneOffset.UTC))){
            logger.error("Registration occur before completion URI issuance. User = {}, Not valid before = {}", command.getUserId(), command.getNotValidBefore());
            throw new RequestException(401, "Cannot complete registration, please restart");
        }

        if (now.isAfter(OffsetDateTime.ofInstant(Instant.ofEpochSecond(command.getNotValidAfter()), ZoneOffset.UTC))){
            logger.error("Registration occur after completion URI expiration. User = {}, Not valid after = {}", command.getUserId(), command.getNotValidAfter());
            throw new RequestException(401, "Cannot complete registration, please restart");
        }

        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        if (user.isEmailVerified()){
            throw new RequestException(400, "User has completed registration");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);
        return null;
    }
}
