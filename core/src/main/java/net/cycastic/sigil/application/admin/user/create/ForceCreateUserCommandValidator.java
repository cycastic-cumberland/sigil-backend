package net.cycastic.sigil.application.admin.user.create;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ForceCreateUserCommandValidator implements CommandValidator<ForceCreateUserCommand, IdDto> {
    private static final Pattern ROLE_PATTERN = Pattern.compile("^[A-Z_]+[0-9]*$");

    @Override
    public void validate(ForceCreateUserCommand command) {
        if (command.getRoles() == null){
            return;
        }

        for (var role : command.getRoles()){
            if (ROLE_PATTERN.matcher(role).matches()){
                continue;
            }

            throw new ValidationException(Map.of("roles", List.of("Role `%s` is invalid".formatted(role))));
        }
    }
}
