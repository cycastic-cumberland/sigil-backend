package net.cycastic.sigil.application.pm.task.update;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.CipherDecryptionMethod;
import org.springframework.stereotype.Component;

@Component
public class UpdateTaskCommandValidator implements CommandValidator<UpdateTaskCommand, Void> {
    @Override
    public void validate(UpdateTaskCommand command) {
        if (!command.getEncryptedName().getDecryptionMethod().equals(CipherDecryptionMethod.UNWRAPPED_PARTITION_KEY)){
            throw new RequestException(400, "Invalid task name's decryption method");
        }
        if (command.getEncryptedName().getIv() == null){
            throw new RequestException(400, "Invalid task name's IV");
        }
        if (command.getEncryptedContent() != null){
            if (!command.getEncryptedContent().getDecryptionMethod().equals(CipherDecryptionMethod.UNWRAPPED_PARTITION_KEY)){
                throw new RequestException(400, "Invalid task content's decryption method");
            }
            if (command.getEncryptedContent().getIv() == null){
                throw new RequestException(400, "Invalid task content's IV");
            }
        }
    }
}
