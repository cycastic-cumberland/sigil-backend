package net.cycastic.sigil.application.partition.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DeletePartitionCommandHandler implements Command.Handler<DeletePartitionCommand, @Null Object> {
    private final PartitionService partitionService;
    private final PartitionRepository partitionRepository;

    @Override
    @Transactional
    public @Null Object handle(DeletePartitionCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE | ApplicationConstants.PartitionPermissions.WRITE);
        var partition = partitionService.getPartition();
        partition.setRemovedAt(OffsetDateTime.now());
        partitionRepository.save(partition);
        return null;
    }
}
