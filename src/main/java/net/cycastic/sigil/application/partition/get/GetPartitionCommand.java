package net.cycastic.sigil.application.partition.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.PartitionDto;

@Data
public class GetPartitionCommand implements Command<PartitionDto> {
    private String partitionPath;
}
