package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.storage.BaseS3Configurations;
import net.cycastic.sigil.configuration.storage.S3Configurations;
import net.cycastic.sigil.service.impl.storage.S3Bucket;
import net.cycastic.sigil.service.storage.BucketProvider;
import net.cycastic.sigil.service.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Lazy
@Component
@RequiredArgsConstructor
public class S3StorageProvider implements AutoCloseable, StorageProvider {
    private final ConcurrentHashMap<String, S3BucketProvider> cachedProviders = new ConcurrentHashMap<>();
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ApplicationContext ctx;

    @Override
    public void close() {
        s3Client.close();
        s3Presigner.close();
    }

    @Component
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    private static class S3BucketProvider implements BucketProvider {
        private final S3Bucket s3Bucket;

        public S3BucketProvider(S3StorageProvider provider, String bucketName){
            s3Bucket = new S3Bucket(provider.s3Client, provider.s3Presigner, bucketName);
        }

        @Override
        @HandleAwsException
        public String generatePresignedUploadPath(String fileKey, String fileName, OffsetDateTime expiration, long objectLength, byte[] encryptionKeyMd5Checksum) {
            return s3Bucket.generatePresignedUploadPath(fileKey, fileName, expiration, objectLength, encryptionKeyMd5Checksum);
        }

        @Override
        @HandleAwsException
        public String generatePresignedDownloadPath(String fileKey, String fileName, OffsetDateTime expiration, @Nullable String encryptionKeyMd5Checksum) {
            return s3Bucket.generatePresignedDownloadPath(fileKey, fileName, expiration, encryptionKeyMd5Checksum);
        }

        @Override
        public void upload(String fileKey, String contentType, long contentLength, Supplier<InputStream> streamSupplier, byte[] decryptionKey) {
            s3Bucket.upload(fileKey, contentType, contentLength, streamSupplier, decryptionKey);;
        }

        @Override
        @HandleAwsException
        public InputStream download(final String fileKey, final String fileName, final byte[] decryptionKey) {
            return s3Bucket.download(fileKey, fileName, decryptionKey);
        }

        @Override
        public boolean exists(String fileKey) {
            return s3Bucket.exists(fileKey);
        }

        @Override
        @HandleAwsException
        public long getObjectSize(String fileKey){
            return s3Bucket.getObjectSize(fileKey);
        }

        @HandleAwsException
        public void deleteFile(String fileKey){
            s3Bucket.deleteFile(fileKey);
        }

        @Override
        @HandleAwsException
        public void copyFile(String sourceFileKey, String destinationFileKey) {
            s3Bucket.copyFile(sourceFileKey, destinationFileKey);
        }
    }

    public static S3Client buildClient(BaseS3Configurations configurations, AwsCredentialsProvider awsCredentialsProvider){
        var builder = S3Client.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(configurations.getRegionName()));
        if (configurations.getServiceUrl() != null){
            builder = builder.endpointOverride(URI.create(configurations.getServiceUrl()))
                    .forcePathStyle(true);
        }

        return builder.build();
    }

    public static S3Presigner buildPresigner(BaseS3Configurations configurations, AwsCredentialsProvider awsCredentialsProvider){
        var builder = S3Presigner.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(configurations.getRegionName()));
        if (configurations.getServiceUrl() != null){
            builder = builder.endpointOverride(URI.create(configurations.getServiceUrl()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }

    @Autowired
    public S3StorageProvider(S3Configurations s3Configurations,
                             AwsCredentialsProvider awsCredentialsProvider,
                             ApplicationContext ctx){
        s3Client = buildClient(s3Configurations, awsCredentialsProvider);
        s3Presigner = buildPresigner(s3Configurations, awsCredentialsProvider);
        this.ctx = ctx;
    }

    @Override
    public BucketProvider getBucket(String bucketName) {
        return cachedProviders.computeIfAbsent(bucketName,
                k -> ctx.getBean(S3BucketProvider.class, this, k));
    }
}
