package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.S3Configurations;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.domain.CryptographicUtilities;
import net.cycastic.sigil.service.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Lazy
@Component
@RequiredArgsConstructor
public class S3StorageProvider implements AutoCloseable, StorageProvider {
    private static final String DEFAULT_SSE_C_ALGORITHM = "AES256";
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
    @RequiredArgsConstructor
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class S3BucketProvider implements BucketProvider {
        private final S3StorageProvider provider;
        private final String bucketName;

        @Override
        @HandleS3Exception
        public String generatePresignedUploadPath(String fileKey, String fileName, OffsetDateTime expiration, long objectLength, byte[] encryptionKeyMd5Checksum) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType("application/octet-stream")
                    .contentLength(objectLength);
            if (encryptionKeyMd5Checksum != null){
                requestBuilder = requestBuilder
                        .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                        .sseCustomerKeyMD5(Base64.getEncoder().encodeToString(encryptionKeyMd5Checksum));
            }

            var request = requestBuilder.build();
            var presignedPut = provider.s3Presigner.presignPutObject(r ->
                    r.signatureDuration(ttl).putObjectRequest(request)
            );
            return presignedPut.url().toString();
        }

        private static GetObjectRequest.Builder withDecryption(GetObjectRequest.Builder builder, byte[] key){
            var encoder = Base64.getEncoder();
            var keyBase64 = encoder.encodeToString(key);
            var md5Base64 = encoder.encodeToString(CryptographicUtilities.digestMd5(key));
            return builder
                    .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                    .sseCustomerKeyMD5(md5Base64)
                    .sseCustomerKey(keyBase64);
        }

        @Override
        @HandleS3Exception
        public String generatePresignedDownloadPath(String fileKey, String fileName, OffsetDateTime expiration, @Nullable String encryptionKeyMd5Checksum) {
            var ttl = Duration.between(OffsetDateTime.now(), expiration);
            var requestBuilder = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .responseContentDisposition("attachment; filename=\"" + ApplicationUtilities.encodeURIComponent(fileName) + "\"");
            if (encryptionKeyMd5Checksum != null){
                requestBuilder = requestBuilder
                        .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                        .sseCustomerKeyMD5(encryptionKeyMd5Checksum);
            }

            var getReq = requestBuilder
                    .build();

            var presignedGet = provider.s3Presigner.presignGetObject(r ->
                    r.signatureDuration(ttl).getObjectRequest(getReq)
            );
            return presignedGet.url().toString();
        }

        @Override
        public void upload(String fileKey, String contentType, long contentLength, Supplier<InputStream> streamSupplier, byte[] decryptionKey) {
            var requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength(contentLength);
            if (decryptionKey != null){
                var keyChecksum = CryptographicUtilities.digestMd5(decryptionKey);
                var decryptionKeyBase64 = Base64.getEncoder().encodeToString(decryptionKey);
                var keyChecksumBase64 = Base64.getEncoder().encodeToString(keyChecksum);
                requestBuilder = requestBuilder
                        .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                        .sseCustomerKeyMD5(keyChecksumBase64)
                        .sseCustomerKey(decryptionKeyBase64);
            }

            provider.s3Client.putObject(requestBuilder.build(), RequestBody.fromContentProvider(streamSupplier::get, contentType));
        }

        @Override
        @HandleS3Exception
        public InputStream download(final String fileKey, final String fileName, final byte[] decryptionKey) {
            var requestBuilder = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .responseContentDisposition("attachment; filename=\"" + ApplicationUtilities.encodeURIComponent(fileName) + "\"");

            if (decryptionKey != null){
                requestBuilder = withDecryption(requestBuilder, decryptionKey);
            }

            var getReq = requestBuilder.build();
            return provider.s3Client.getObject(getReq, ResponseTransformer.toInputStream());
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
