package net.cycastic.sigil.application.partition.delete;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.cache.EvictCacheBackgroundJob;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.controller.PartitionsController;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.repository.listing.PartitionRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.job.JobScheduler;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DeletePartitionCommandHandler implements Command.Handler<DeletePartitionCommand, Void> {
    private final PartitionService partitionService;
    private final PartitionRepository partitionRepository;
    private final JobScheduler jobScheduler;
    private final LoggedUserAccessor loggedUserAccessor;

    @Override
    public Void handle(DeletePartitionCommand command) {
        partitionService.checkPermission(ApplicationConstants.PartitionPermissions.MODERATE | ApplicationConstants.PartitionPermissions.WRITE);
        var partition = partitionService.getPartition();
        partition.setRemovedAt(OffsetDateTime.now());
        partitionRepository.save(partition);

        jobScheduler.deferInfallible(EvictCacheBackgroundJob.builder()
                .cacheKey(PartitionsController.CACHE_KEY)
                .cacheName("getPartition?tenantId=%d&userId=%d&partitionPath=%s".formatted(loggedUserAccessor.getTenantId(),
                        loggedUserAccessor.getUserId(),
                        partition.getPartitionPath()))
                .build());
        jobScheduler.deferInfallible(EvictCacheBackgroundJob.builder()
                .cacheKey(PartitionsController.CACHE_KEY)
                .cacheName("getPartition?tenantId=%d&userId=%d&id=%d".formatted(loggedUserAccessor.getTenantId(),
                        loggedUserAccessor.getUserId(),
                        partition.getId()))
                .build());
        return null;
    }
}
