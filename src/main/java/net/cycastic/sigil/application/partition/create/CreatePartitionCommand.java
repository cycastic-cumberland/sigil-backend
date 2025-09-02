package net.cycastic.sigil.application.partition.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.TransactionalCommand;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.model.listing.PartitionType;
import jakarta.annotation.Nullable;

@Data
@TransactionalCommand
public class CreatePartitionCommand implements Command<IdDto> {
    @NotEmpty
    private String partitionPath;

    private boolean serverSideKeyDerivation;

    @Nullable
    private PartitionType partitionType;

    @Nullable
    private CreateProjectPartitionPayload projectPartition;
}
