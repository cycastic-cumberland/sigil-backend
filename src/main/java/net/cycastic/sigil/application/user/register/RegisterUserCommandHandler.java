package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RegisterUserCommandHandler implements Command.Handler<RegisterUserCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @SneakyThrows
    public Void handle(RegisterUserCommand command) {
        var userOpt = userRepository.getByEmail(command.getEmail());
        if (userOpt.isPresent()){
            var user = userOpt.get();
            if (user.getStatus() == UserStatus.INVITED){
                userService.sendConfirmationEmailNoTransaction(user);
            }
            return null;
        }

        var user = userService.registerUserNoTransaction(command.getEmail(),
                Collections.singleton(ApplicationConstants.Roles.COMMON),
                UserStatus.INVITED,
                false);
        userService.sendConfirmationEmailNoTransaction(user);
        return null;
    }
}
