package net.cycastic.sigil.application.partition.get;


import net.cycastic.sigil.application.validation.CommandValidator;
import net.cycastic.sigil.domain.dto.listing.PartitionDto;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetPartitionCommandValidator implements CommandValidator<GetPartitionCommand, PartitionDto> {
    @Override
    public void validate(GetPartitionCommand command) {
        try {
            Objects.requireNonNullElse(command.getId(), command.getPartitionPath());
        } catch (NullPointerException e){
            throw new RequestException(400, "Either id or partitionPath must be supplied");
        }
    }
}
