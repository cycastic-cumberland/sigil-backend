package net.cycastic.sigil.application.partition.create;

import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.application.validation.JakartaValidationHelper;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreatePartitionCommandValidator implements CommandValidator<CreatePartitionCommand, IdDto> {
    @Override
    public void validate(CreatePartitionCommand command) {
        if (Objects.requireNonNullElse(command.getPartitionType(), PartitionType.GENERIC).equals(PartitionType.PROJECT)){
            if (command.isServerSideKeyDerivation()){
                throw new RequestException(400, "SAE is not supported for project type partitions");
            }
            JakartaValidationHelper.validateObject(command.getProjectPartition());
        }
    }
}
