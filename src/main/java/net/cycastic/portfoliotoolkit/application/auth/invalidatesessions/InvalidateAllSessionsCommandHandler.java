package net.cycastic.portfoliotoolkit.application.auth.invalidatesessions;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.auth.UserService;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvalidateAllSessionsCommandHandler implements Command.Handler<InvalidateAllSessionsCommand, @Null Object> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;

    @Override
    public @Null Object handle(InvalidateAllSessionsCommand command) {
        if (loggedUserAccessor.getClaims() == null){
            throw new RequestException(401, "User not signed in");
        }
        var currentUserId = loggedUserAccessor.getUserId();
        if (!loggedUserAccessor.isAdmin() &&
            currentUserId != command.getUserId()){
            throw new ForbiddenException();
        }

        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "Could not find user"));
        UserService.refreshSecurityStamp(user);
        userRepository.save(user);
        return null;
    }
}
