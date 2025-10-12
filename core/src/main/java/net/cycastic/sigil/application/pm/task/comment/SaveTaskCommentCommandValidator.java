package net.cycastic.sigil.application.pm.task.comment;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.application.validation.JakartaValidationHelper;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

@Component
public class SaveTaskCommentCommandValidator implements CommandValidator<SaveTaskCommentCommand, IdDto> {
    @Override
    public void validate(SaveTaskCommentCommand command) {
        if (command.getId() == null){
            if (command.getTaskId() == null){
                throw new RequestException(400, "taskId must not be null");
            }
            JakartaValidationHelper.validateObject(command.getEncryptedContent());
            return;
        }

        JakartaValidationHelper.validateObject(command.getEncryptedContent());
    }
}
