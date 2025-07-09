package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.configuration.RegistrationConfigurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.UsageType;
import net.cycastic.sigil.domain.model.UserStatus;
import net.cycastic.sigil.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RegisterUserCommandHandler implements Command.Handler<RegisterUserCommand, @Null Object> {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RegistrationConfigurations registrationConfigurations;

    @Override
    @Transactional
    public Object handle(RegisterUserCommand command) {
        if (registrationConfigurations.getRegistrationLinkValidSeconds() <= 0){
            throw new IllegalStateException("Invalid registration expiration time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }
        if (registrationConfigurations.getResendVerificationLimitSeconds() <= 0){
            throw new IllegalStateException("Invalid resend verification time: " + registrationConfigurations.getRegistrationLinkValidSeconds());
        }

        var user = userRepository.getByEmail(command.getEmail());
        if (user != null){
            if (user.getStatus() != UserStatus.INVITED || command.getPassword() != null || command.getFirstName() != null || command.getLastName() != null){
                throw new RequestException(409, "This email is used");
            }

            var now = OffsetDateTime.now();
            if (user.getLastInvitationSent() != null){
                var secondsElapsed = Duration.between(user.getLastInvitationSent(), now).getSeconds();
                if (secondsElapsed < registrationConfigurations.getResendVerificationLimitSeconds()){
                    throw new RequestException(400, "Please retry in %d second(s)", registrationConfigurations.getResendVerificationLimitSeconds() - secondsElapsed);
                }
            }
            user.setLastInvitationSent(now);
            userRepository.save(user);
            userService.sendConfirmationEmail(user);
            return null;
        }

        user = userService.registerUserNoTransaction(command.getEmail(),
                command.getFirstName(),
                command.getLastName(),
                command.getPassword(),
                Collections.singleton(ApplicationConstants.Roles.COMMON),
                UserStatus.INVITED,
                UsageType.STANDARD,
                false);
        userService.sendConfirmationEmail(user);
        return null;
    }
}
