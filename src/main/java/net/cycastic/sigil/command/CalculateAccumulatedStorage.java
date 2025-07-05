package net.cycastic.sigil.command;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.model.Tenant;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.sigil.service.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Collection;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@CommandLine.Command(name = "calc-storage", mixinStandardHelpOptions = true, description = "Calculate accumulated storage")
public class CalculateAccumulatedStorage implements Callable<Integer> {
    private static final int BATCH_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(CalculateAccumulatedStorage.class);

    private final TenantRepository tenantRepository;
    private final StorageProvider storageProvider;
    private final AttachmentListingRepository attachmentListingRepository;

    @SneakyThrows
    private static Long getLong(Future<Long> future){
        return future.get();
    }

    @SneakyThrows
    private void calculateSingleUser(Tenant tenant, ExecutorService executorService){
        var total = 0L;
        for (var i = 0;; i++){
            var page = attachmentListingRepository.getObjectKeysByUser(tenant,
                    PageRequest.of(i, BATCH_SIZE, Sort.by("id")));
            var contents = page.getContent();
            if (contents.isEmpty()){
                break;
            }
            logger.info("Calculating. Project: {}. Batch: {}/{}", tenant.getId(), i + 1, page.getTotalPages());

            Collection<Callable<Long>> delegates = contents.stream()
                    .map(o -> (Callable<Long>)(() -> storageProvider.getBucket(o.getBucket()).getObjectSize(o.getKey())))
                    .toList();
            var futures = executorService.invokeAll(delegates);

            long pageTotal = futures.stream()
                    .mapToLong(CalculateAccumulatedStorage::getLong)
                    .sum();
            total += pageTotal;
        }

        tenant.setAccumulatedAttachmentStorageUsage(total);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public Integer call() {
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()){
            for (var i = 0;; i++){
                var page = tenantRepository.findAll(PageRequest.of(i, BATCH_SIZE, Sort.by("id")));
                var contents = page.getContent();
                if (contents.isEmpty()){
                    break;
                }
                logger.info("Querying users. Batch: {}/{}", i + 1, page.getTotalPages());

                for (var user : contents){
                    calculateSingleUser(user, executorService);
                }
            }
        }
        return 0;
    }
}
