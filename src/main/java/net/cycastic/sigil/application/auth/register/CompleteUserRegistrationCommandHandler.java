package net.cycastic.sigil.application.auth.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.auth.UserService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteUserRegistrationCommandHandler implements Command.Handler<CompleteUserRegistrationCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Void handle(CompleteUserRegistrationCommand command) {
        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        if (user.isEmailVerified()){
            throw new RequestException(400, "User has completed registration");
        }

        user.setEmailVerified(true);
        userService.completeRegistrationNoTransaction(user, command.getForm());
        user.setStatus(UserStatus.ACTIVE);
        return null;
    }
}
