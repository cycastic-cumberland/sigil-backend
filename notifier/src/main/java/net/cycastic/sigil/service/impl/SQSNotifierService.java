package net.cycastic.sigil.service.impl;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.FilesUtilities;
import net.cycastic.sigil.domain.dto.notification.EmailContentRegistryDto;
import net.cycastic.sigil.domain.dto.notification.EmailNotificationRequestDto;
import net.cycastic.sigil.domain.dto.notification.NotificationRequestDto;
import net.cycastic.sigil.service.email.EmailImage;
import net.cycastic.sigil.service.email.EmailSender;
import net.cycastic.sigil.service.impl.storage.S3Bucket;
import net.cycastic.sigil.service.impl.storage.ZipCompressUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SQSNotifierService {
    private static final Logger logger = LoggerFactory.getLogger(SQSNotifierService.class);
    private final EmailSender emailSender;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    private void processEmailNotification(EmailNotificationRequestDto request){
        var tempDir = FilesUtilities.getTempFile();
        Files.createDirectories(tempDir);
        try (var client = S3Client.builder()
                .region(Region.of(request.getRegion()))
                .build()) {
            String htmlBody;
            Map<String, EmailImage> images;
            var bucket = new S3Bucket(client, null, request.getBucketName());
            try (var zipStream = bucket.download(request.getKey(), Paths.get(request.getKey()).getFileName().toString(), null)){
                ZipCompressUtility.INSTANCE.decompressFolder(tempDir, zipStream);
            }

            final var decompressPath = tempDir.resolve(ApplicationConstants.REMOTE_SQS_DIRECTORY_ROOT);
            var registry = objectMapper.readValue(decompressPath.resolve("registry.json"), EmailContentRegistryDto.class);
            htmlBody = Files.readString(decompressPath.resolve(registry.getIndexRelativePath()));
            images = HashMap.newHashMap(registry.getImageEntries().size());
            for (var entry : registry.getImageEntries()){
                final var e = entry;
                images.put(entry.getName(), new EmailImage() {
                    @Override
                    public String getFileName() {
                        return e.getName();
                    }

                    @Override
                    public String getMimeType() {
                        return e.getMimeType();
                    }

                    @Override
                    public InputStreamSource getImageSource() {
                        return () -> Files.newInputStream(decompressPath.resolve(e.getRelativePath()), StandardOpenOption.READ);
                    }
                });
            }
            emailSender.sendHtml(request.getFromAddress(),
                    request.getFromName(),
                    request.getTo(),
                    request.getCc(),
                    request.getSubject(),
                    htmlBody,
                    images);
        } finally {
            FilesUtilities.deleteRecursively(tempDir);
        }
    }

    public SQSBatchResponse process(SQSEvent event){
        var errors = new ArrayList<SQSBatchResponse.BatchItemFailure>();
        for (var record : event.getRecords()){
            try {
                var request = objectMapper.readValue(record.getBody(), NotificationRequestDto.class);
                switch (request.getType()){
                    case EMAIL -> processEmailNotification(objectMapper.readValue(record.getBody(), EmailNotificationRequestDto.class));
                    case APP -> throw new RuntimeException("Unimplemented");
                    default -> throw new IllegalStateException("Illegal request type");
                }
            } catch (Exception e){
                logger.error("Failed to process event for message {}", record.getMessageId(), e);
                errors.add(new SQSBatchResponse.BatchItemFailure(record.getMessageId()));
            }
        }
        return errors.isEmpty() ? new SQSBatchResponse() : new SQSBatchResponse(errors);
    }
}
