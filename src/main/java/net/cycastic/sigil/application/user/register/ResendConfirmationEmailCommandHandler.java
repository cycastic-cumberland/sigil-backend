package net.cycastic.sigil.application.user.register;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResendConfirmationEmailCommandHandler implements Command.Handler<ResendConfirmationEmailCommand, Void> {
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Void handle(ResendConfirmationEmailCommand command) {
        var userOpt = userRepository.getByEmail(command.getEmail());
        if (userOpt.isEmpty() || userOpt.get().isEmailVerified()){
            return null;
        }

        userService.sendConfirmationEmailNoTransaction(userOpt.get());
        return null;
    }
}
