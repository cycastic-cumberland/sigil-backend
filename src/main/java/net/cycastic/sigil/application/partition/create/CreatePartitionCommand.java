package net.cycastic.sigil.application.partition.create;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.IdDto;

@Data
public class CreatePartitionCommand implements Command<IdDto> {
    private String partitionPath;
    private boolean serverSideKeyDerivation;
}
