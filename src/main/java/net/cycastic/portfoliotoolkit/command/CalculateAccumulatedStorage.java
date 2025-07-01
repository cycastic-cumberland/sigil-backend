package net.cycastic.portfoliotoolkit.command;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.domain.model.User;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@CommandLine.Command(name = "calc-storage", mixinStandardHelpOptions = true, description = "Calculate accumulated storage")
public class CalculateAccumulatedStorage implements Callable<Integer> {
    private static final int BATCH_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(CalculateAccumulatedStorage.class);

    @CommandLine.Option(names = "--email")
    private @Nullable String userEmail;

    private final UserRepository userRepository;
    private final StorageProvider storageProvider;
    private final AttachmentListingRepository attachmentListingRepository;

    @SneakyThrows
    private static Long getLong(Future<Long> future){
        return future.get();
    }

    @SneakyThrows
    private void calculateSingleUser(User user, ExecutorService executorService){
        var total = 0L;
        for (var i = 0;; i++){

            var page = attachmentListingRepository.getObjectKeysByUser(user,
                    PageRequest.of(i, BATCH_SIZE, Sort.by("id")));
            var contents = page.getContent();
            if (contents.isEmpty()){
                break;
            }
            logger.info("Calculating. User: {}. Batch: {}/{}", user.getEmail(), i + 1, page.getTotalPages());

            Collection<Callable<Long>> delegates = contents.stream()
                    .map(o -> (Callable<Long>)(() -> storageProvider.getBucket(o.getBucket()).getObjectSize(o.getKey())))
                    .toList();
            var futures = executorService.invokeAll(delegates);

            long pageTotal = futures.stream()
                    .mapToLong(CalculateAccumulatedStorage::getLong)
                    .sum();
            total += pageTotal;
        }

        user.setAccumulatedAttachmentStorageUsage(total);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Integer call() {
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()){
            if (userEmail != null){
                var user = Objects.requireNonNull(userRepository.getByEmail(userEmail));
                calculateSingleUser(user, executorService);
            } else {
                for (var i = 0;; i++){
                    var page = userRepository.findAll(PageRequest.of(i, BATCH_SIZE, Sort.by("id")));
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
        }
        return 0;
    }
}
