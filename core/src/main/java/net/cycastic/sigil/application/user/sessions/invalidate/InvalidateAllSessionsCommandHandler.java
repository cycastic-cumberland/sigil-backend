package net.cycastic.sigil.application.user.sessions.invalidate;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvalidateAllSessionsCommandHandler implements Command.Handler<InvalidateAllSessionsCommand, Void> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;

    @Override
    public Void handle(InvalidateAllSessionsCommand command) {
        if (loggedUserAccessor.getClaims() == null){
            throw new RequestException(401, "User not signed in");
        }
        var currentUserId = loggedUserAccessor.getUserId();
        if (!loggedUserAccessor.isAdmin() &&
            currentUserId != command.getUserId()){
            throw RequestException.forbidden();
        }

        var user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new RequestException(404, "Could not find user"));
        UserService.refreshSecurityStamp(user);
        userRepository.save(user);
        return null;
    }
}
