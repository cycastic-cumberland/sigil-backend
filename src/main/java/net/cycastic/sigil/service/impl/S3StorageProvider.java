package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.S3Configurations;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Lazy
@Component
@RequiredArgsConstructor
public class S3StorageProvider implements StorageProvider {
    private final ConcurrentHashMap<String, S3BucketProvider> cachedProviders = new ConcurrentHashMap<>();
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ApplicationContext ctx;

    @Component
    @RequiredArgsConstructor
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class S3BucketProvider implements BucketProvider {
        private final S3StorageProvider provider;
        private final String bucketName;

        @Override
        @HandleS3Exception
        public String generatePresignedUploadPath(String fileKey, String fileName, OffsetDateTime expiration, long objectLength) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType("application/octet-stream")
                    .contentLength(objectLength);

            var presignedPut = provider.s3Presigner.presignPutObject(r ->
                    r.signatureDuration(ttl)
                            .putObjectRequest(requestBuilder.build())
            );
            return presignedPut.url().toString();
        }

        @Override
        @HandleS3Exception
        public String generatePresignedDownloadPath(String fileKey, String fileName, OffsetDateTime expiration) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var getReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .responseContentDisposition("attachment; filename=\"" + ApplicationUtilities.encodeURIComponent(fileName) + "\"")
                    .build();

            var presignedGet = provider.s3Presigner.presignGetObject(r ->
                    r.signatureDuration(ttl).getObjectRequest(getReq)
            );
            return presignedGet.url().toString();
        }

        @Override
        @HandleS3Exception
        public InputStream openDownloadStream(String fileKey) {
            return provider.s3Client.getObject(request -> request
                            .bucket(bucketName)
                            .key(fileKey),
                    ResponseTransformer.toInputStream());
        }

        private HeadObjectResponse headObject(String fileKey){
            return provider.s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build());
        }

        @Override
        public boolean exists(String fileKey) {
            try {
                headObject(fileKey);
                return true;
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    return false;
                }
                throw e;
            }
        }

        @Override
        @HandleS3Exception
        public long getObjectSize(String fileKey){
            return headObject(fileKey).contentLength();
        }

        @HandleS3Exception
        public void deleteFile(String fileKey){
            provider.s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .build());
        }

        @Override
        @HandleS3Exception
        public void copyFile(String sourceFileKey, String destinationFileKey) {
            var copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceFileKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationFileKey)
                    .build();

            provider.s3Client.copyObject(copyRequest);
        }
    }

    private static S3Client buildClient(S3Configurations configurations){
        var builder = S3Client.builder()
                .region(Region.of(configurations.getRegionName()));
        if (configurations.getAccessKey() != null){
            var credentials = AwsBasicCredentials.create(configurations.getAccessKey(),
                    configurations.getSecretKey());
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }
        if (configurations.getServiceUrl() != null){
            builder = builder.endpointOverride(URI.create(configurations.getServiceUrl()))
                    .forcePathStyle(true);
        }

        return builder.build();
    }

    private static S3Presigner buildPresigner(S3Configurations configurations){
        var builder = S3Presigner.builder()
                .region(Region.of(configurations.getRegionName()));
        if (configurations.getAccessKey() != null){
            var credentials = AwsBasicCredentials.create(configurations.getAccessKey(),
                    configurations.getSecretKey());
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }
        if (configurations.getServiceUrl() != null){
            builder = builder.endpointOverride(URI.create(configurations.getServiceUrl()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }

    @Autowired
    public S3StorageProvider(S3Configurations s3Configurations, ApplicationContext ctx){
        s3Client = buildClient(s3Configurations);
        s3Presigner = buildPresigner(s3Configurations);
        this.ctx = ctx;
    }

    @Override
    public BucketProvider getBucket(String bucketName) {
        return cachedProviders.computeIfAbsent(bucketName,
                k -> ctx.getBean(S3BucketProvider.class, this, k));
    }
}
