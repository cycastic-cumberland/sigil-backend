package net.cycastic.sigil.application.pm.task.update;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

@Component
public class UpdateTaskCommandValidator implements CommandValidator<UpdateTaskCommand, Void> {
    private static boolean bothNullOrNonNull(Object lhs, Object rhs){
        return (lhs == null && rhs == null) || (lhs != null && rhs != null);
    }

    @Override
    public void validate(UpdateTaskCommand command) {
        if (!bothNullOrNonNull(command.getIv(), command.getEncryptedName()) || !bothNullOrNonNull(command.getIv(), command.getEncryptedContent())){
            throw new RequestException(400, "iv, encryptedName and encryptedContent must be present");
        }
    }
}
