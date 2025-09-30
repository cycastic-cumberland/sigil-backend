package net.cycastic.sigil.service.impl.email;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.mail.SqsRemoteEmailConfigurations;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.FilesUtilities;
import net.cycastic.sigil.domain.dto.notification.EmailContentRegistryDto;
import net.cycastic.sigil.domain.dto.notification.EmailImageEntryDto;
import net.cycastic.sigil.domain.dto.notification.EmailNotificationRequestDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.service.email.DeferredEmailSender;
import net.cycastic.sigil.service.email.EmailImage;
import net.cycastic.sigil.service.impl.S3StorageProvider;
import net.cycastic.sigil.service.impl.storage.S3Bucket;
import net.cycastic.sigil.service.impl.storage.ZipCompressUtility;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SqsRemoteEmailSender implements DeferredEmailSender, AutoCloseable {
    private final JsonSerializer jsonSerializer;
    private final SqsClient sqsClient;
    private final SqsRemoteEmailConfigurations configurations;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final String senderAddress;

    public SqsRemoteEmailSender(String senderAddress,
                                JsonSerializer jsonSerializer,
                                SqsRemoteEmailConfigurations configurations,
                                AwsCredentialsProvider awsCredentialsProvider){
        this.jsonSerializer = jsonSerializer;
        this.senderAddress = senderAddress;
        this.configurations = configurations;
        this.awsCredentialsProvider = awsCredentialsProvider;
        var builder = SqsClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(configurations.getRegionName()));
        sqsClient = builder.build();
    }

    @Override
    @SneakyThrows
    public void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody, @Nullable Map<String, ? extends EmailImage> imageStreamSource) {
        var tempDir = FilesUtilities.getTempFile();
        try {
            var tempDirRootEnvelop = tempDir.resolve("envelop");
            var tempDirRoot = tempDirRootEnvelop.resolve(ApplicationConstants.REMOTE_SQS_DIRECTORY_ROOT);
            var assetsDirRoot = tempDirRoot.resolve("assets");

            Files.createDirectories(tempDir);
            Files.createDirectories(tempDirRootEnvelop);
            Files.createDirectories(tempDirRoot);
            Files.createDirectories(assetsDirRoot);
            Files.writeString(tempDirRoot.resolve("index.html"), htmlBody);

            var imageEntries = new ArrayList<EmailImageEntryDto>(imageStreamSource == null ? 0 : imageStreamSource.size());
            if (imageStreamSource != null){
                for (var entry : imageStreamSource.entrySet()){
                    var entryPath = assetsDirRoot.resolve(UUID.randomUUID().toString());
                    imageEntries.add(EmailImageEntryDto.builder()
                            .mappingName(entry.getKey())
                            .name(entry.getValue().getFileName())
                            .mimeType(entry.getValue().getMimeType())
                            .relativePath(tempDirRoot.relativize(entryPath).toString())
                            .build());

                    try (var outStream = Files.newOutputStream(entryPath, StandardOpenOption.CREATE_NEW)){
                        try (var inStream = entry.getValue().getImageSource().getInputStream()){
                            inStream.transferTo(outStream);
                        }
                    }
                }
            }

            var registry = EmailContentRegistryDto.builder()
                    .indexRelativePath("index.html")
                    .imageEntries(imageEntries)
                    .build();

            Files.writeString(tempDirRoot.resolve("registry.json"), jsonSerializer.serialize(registry));

            var zipFile = tempDir.resolve("attachment.zip");
            try (var outStream = Files.newOutputStream(zipFile, StandardOpenOption.CREATE_NEW)){
                ZipCompressUtility.INSTANCE.compressFolder(tempDirRootEnvelop, outStream);
            }

            var fileKey = Path.of("__tmp", ApplicationUtilities.shardObjectKey(UUID.randomUUID() + ".zip")).toString();
            try (var client = S3StorageProvider.buildClient(configurations.getBucket(), awsCredentialsProvider)){
                var bucket = new S3Bucket(client, null, configurations.getBucket().getAttachmentBucketName());
                bucket.upload(fileKey,
                        "application/zip",
                        Files.size(zipFile),
                        new Supplier<>() {
                            @Override
                            @SneakyThrows
                            public InputStream get() {
                                return Files.newInputStream(zipFile, StandardOpenOption.READ);
                            }
                        },
                        null);
            }


            var request = EmailNotificationRequestDto.builder()
                    .fromAddress(fromAddress)
                    .fromName(fromName)
                    .to(to)
                    .cc(cc)
                    .subject(subject)
                    .region(configurations.getBucket().getRegionName())
                    .bucketName(configurations.getBucket().getAttachmentBucketName())
                    .key(fileKey)
                    .build();
            var serializedRequest = jsonSerializer.serialize(request);
            var sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(configurations.getQueueUrl())
                    .messageBody(serializedRequest)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
        } catch (AwsServiceException e) {
            var statusCode = e.statusCode();
            switch (statusCode){
                case 400:
                case 401:
                case 403:
                case 404: {
                    throw new RequestException(statusCode, e, e.getMessage());
                }
                default: {
                    throw new RequestException(500, e, "Internal server error");
                }
            }
        } finally {
            FilesUtilities.deleteRecursively(tempDir);
        }
    }

    @Override
    public void sendHtml(String to, String cc, String subject, String htmlBody, @Nullable Map<String, EmailImage> imageStreamSource) {
        sendHtml(senderAddress, "Sigil", to, cc, subject, htmlBody, imageStreamSource);
    }

    @Override
    public void close() {
        sqsClient.close();
    }
}
