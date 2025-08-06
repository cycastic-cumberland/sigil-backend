package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CompleteUserRegistrationCommandHandler implements Command.Handler<CompleteUserRegistrationCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Void handle(CompleteUserRegistrationCommand command) {
        var user = userRepository.findById(command.getQueryParams().getUserId())
                .stream()
                .filter(u -> u.getStatus() != UserStatus.DISABLED)
                .findFirst()
                .orElseThrow(() -> new RequestException(404, "User not found"));

        if (user.isEmailVerified()){
            throw new RequestException(400, "User has completed registration");
        }
        if (!Arrays.equals(Base64.getDecoder().decode(command.getQueryParams().getSecurityStamp()), user.getSecurityStamp())){
            throw RequestException.forbidden();
        }

        userService.completeRegistrationNoTransaction(user, command.getForm());
        return null;
    }
}
