package net.cycastic.sigil.application.user;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendConfirmationEmailJobHandler implements BackgroundJobHandler<SendConfirmationEmailJob> {
    private static final Logger logger = LoggerFactory.getLogger(SendConfirmationEmailJobHandler.class);
    private final UserService userService;

    @Override
    public void process(SendConfirmationEmailJob data) {
        userService.sendConfirmationEmailInternal(data.getUser(), data.getSecurityStamp());
        logger.debug("Confirmation email sent to user {}", data.getUser().getId());
    }
}
