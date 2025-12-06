package net.cycastic.sigil.application.admin.user.update.details;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UpdateUserDetailsCommandHandler implements Command.Handler<UpdateUserDetailsCommand, Void> {
    private final UserRepository userRepository;

    private static List<String> getDefaultRoles(){
        return List.of();
    }

    @Override
    public Void handle(UpdateUserDetailsCommand command) {
        var user = userRepository.findById(command.getId())
                .orElseThrow(() -> new RequestException(404, "User not found"));

        if (!user.getEmail().equalsIgnoreCase(command.getEmail())){
            user.setEmail(command.getEmail());
            user.setNormalizedEmail(command.getEmail().toUpperCase(Locale.ROOT));
        }
        user.setFirstName(command.getFirstName());
        user.setLastName(command.getLastName());
        user.setEmailVerified(command.isEmailVerified());
        user.setStatus(command.getStatus());
        user.setRoles(String.join(",", Stream.concat(Objects.requireNonNullElseGet(command.getRoles(), UpdateUserDetailsCommandHandler::getDefaultRoles).stream(),
                        Stream.of(ApplicationConstants.Roles.COMMON))
                .distinct()
                .sorted()
                .toList()));

        userRepository.save(user);
        return null;
    }
}
