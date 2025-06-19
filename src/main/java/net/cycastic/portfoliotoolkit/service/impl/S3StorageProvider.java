package net.cycastic.portfoliotoolkit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.S3Configurations;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.OutputStream;
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

    @RequiredArgsConstructor
    public static class S3BucketProvider implements BucketProvider {
        private final S3StorageProvider provider;
        private final String bucketName;

        @Override
        public String generatePresignedUploadPath(String fileKey, String fileName, OffsetDateTime expiration) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType("application/octet-stream")
                    .build();

            var presignedPut = provider.s3Presigner.presignPutObject(r ->
                    r.signatureDuration(ttl)
                            .putObjectRequest(putReq)
            );
            return presignedPut.url().toString();
        }

        @Override
        public String generatePresignedDownloadPath(String fileKey, String fileName, OffsetDateTime expiration) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var getReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                    .build();

            var presignedGet = provider.s3Presigner.presignGetObject(r ->
                    r.signatureDuration(ttl).getObjectRequest(getReq)
            );
            return presignedGet.url().toString();
        }

        @Override
        @SneakyThrows
        public void downloadFile(String fileKey, OutputStream stream) {
            var responseStream = provider.s3Client.getObject(request -> request
                                    .bucket(bucketName)
                                    .key(fileKey),
                    ResponseTransformer.toInputStream());
            responseStream.transferTo(stream);
        }

        @Override
        public boolean exists(String fileKey) {
            try {
                provider.s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build());
                return true;
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    return false;
                }
                throw e;
            }
        }
    }

    @Autowired
    public S3StorageProvider(S3Configurations s3Configurations){
        var credentials = AwsBasicCredentials.create(s3Configurations.getAccessKey(),
                s3Configurations.getSecretKey());
        s3Client = S3Client.builder()
                .region(Region.of(s3Configurations.getRegionName()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        s3Presigner = S3Presigner.builder()
                .region(Region.of(s3Configurations.getRegionName()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public BucketProvider getBucket(String bucketName) {
        return cachedProviders.computeIfAbsent(bucketName,
                k -> new S3BucketProvider(this, k));
    }
}
