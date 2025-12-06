package net.cycastic.sigil.application.admin.user.create;


import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.user.UserService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.tenant.UserStatus;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ForceCreateUserCommandHandler implements Command.Handler<ForceCreateUserCommand, IdDto> {
    private final UserRepository userRepository;
    private final UserService userService;

    private static List<String> getDefaultRoles(){
        return List.of();
    }

    @Override
    public IdDto handle(ForceCreateUserCommand command) {
        var userOpt = userRepository.getByEmail(command.getEmail());
        if (userOpt.isPresent()){
            throw RequestException.withExceptionCode("C409T002");
        }

        var user = userService.registerUserNoTransaction(command.getEmail(),
                Stream.concat(Objects.requireNonNullElseGet(command.getRoles(), ForceCreateUserCommandHandler::getDefaultRoles).stream(),
                        Stream.of(ApplicationConstants.Roles.COMMON))
                        .distinct()
                        .sorted()
                        .toList(),
                UserStatus.INVITED,
                false);
        userService.completeRegistrationNoTransaction(user, command.getForm());

        user.setEmailVerified(command.isEmailVerified());
        user.setStatus(Objects.requireNonNullElse(command.getStatus(), UserStatus.ACTIVE));

        userRepository.save(user);
        userService.invalidateAllUserAuthCache(user);

        return new IdDto(user.getId());
    }
}
