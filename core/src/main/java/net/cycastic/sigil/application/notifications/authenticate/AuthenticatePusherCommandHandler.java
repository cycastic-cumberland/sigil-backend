package net.cycastic.sigil.application.notifications.authenticate;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.dto.notifications.PusherAuthenticationDataDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.exception.UnreachableError;
import net.cycastic.sigil.domain.repository.notifications.NotificationTokenRepository;
import net.cycastic.sigil.service.impl.notification.PusherNotificationSender;
import net.cycastic.sigil.service.notification.NotificationSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticatePusherCommandHandler implements Command.Handler<AuthenticatePusherCommand, PusherAuthenticationDataDto> {
    private final NotificationSender notificationSender;
    private final UserService userService;
    private final NotificationTokenRepository notificationTokenRepository;

    @Override
    public PusherAuthenticationDataDto handle(AuthenticatePusherCommand command) {
        if (!(notificationSender instanceof PusherNotificationSender pusherNotificationSender)){
            throw new IllegalStateException("Failed to authenticate pusher as it was not set up");
        }
        if (!command.getChannelName().startsWith("private-")){
            throw new RequestException(400, "Invalid request");
        }

        var uuid = UUID.fromString(command.getNotificationTokenString());
        var notificationToken = notificationTokenRepository.findById(uuid)
                .orElseThrow(() -> new RequestException(400, "Invalid notification token"));
        switch (notificationToken.getConsumer()){
            case USER -> {
                var user = userService.getUser();
                if (!user.getNotificationToken().getToken().equals(uuid)){
                    throw RequestException.forbidden();
                }

                return pusherNotificationSender.authenticate(command.getSocketId(),
                        command.getChannelName());
            }
            default -> throw new UnreachableError("Unimplemented", null);
        }
    }
}
