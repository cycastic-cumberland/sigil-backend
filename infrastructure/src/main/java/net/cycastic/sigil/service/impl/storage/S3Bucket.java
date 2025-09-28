package net.cycastic.sigil.service.impl.storage;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.UrlUtilities;
import net.cycastic.sigil.domain.SlimCryptographicUtilities;
import net.cycastic.sigil.service.storage.BucketProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class S3Bucket implements BucketProvider {
    private static final String DEFAULT_SSE_C_ALGORITHM = "AES256";
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    @Override
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
        var presignedPut = s3Presigner.presignPutObject(r ->
                r.signatureDuration(ttl).putObjectRequest(request)
        );
        return presignedPut.url().toString();
    }

    private static GetObjectRequest.Builder withDecryption(GetObjectRequest.Builder builder, byte[] key){
        var encoder = Base64.getEncoder();
        var keyBase64 = encoder.encodeToString(key);
        var md5Base64 = encoder.encodeToString(SlimCryptographicUtilities.digestMd5(key));
        return builder
                .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                .sseCustomerKeyMD5(md5Base64)
                .sseCustomerKey(keyBase64);
    }

    @Override
    public String generatePresignedDownloadPath(String fileKey, String fileName, OffsetDateTime expiration, @Nullable String encryptionKeyMd5Checksum) {
        var ttl = Duration.between(OffsetDateTime.now(), expiration);
        var requestBuilder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .responseContentDisposition("attachment; filename=\"" + UrlUtilities.encodeURIComponent(fileName) + "\"");
        if (encryptionKeyMd5Checksum != null){
            requestBuilder = requestBuilder
                    .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                    .sseCustomerKeyMD5(encryptionKeyMd5Checksum);
        }

        var getReq = requestBuilder
                .build();

        var presignedGet = s3Presigner.presignGetObject(r ->
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
            var keyChecksum = SlimCryptographicUtilities.digestMd5(decryptionKey);
            var decryptionKeyBase64 = Base64.getEncoder().encodeToString(decryptionKey);
            var keyChecksumBase64 = Base64.getEncoder().encodeToString(keyChecksum);
            requestBuilder = requestBuilder
                    .sseCustomerAlgorithm(DEFAULT_SSE_C_ALGORITHM)
                    .sseCustomerKeyMD5(keyChecksumBase64)
                    .sseCustomerKey(decryptionKeyBase64);
        }

        s3Client.putObject(requestBuilder.build(), RequestBody.fromContentProvider(streamSupplier::get, contentType));
    }

    @Override
    public InputStream download(final String fileKey, final String fileName, final byte[] decryptionKey) {
        var requestBuilder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .responseContentDisposition("attachment; filename=\"" + UrlUtilities.encodeURIComponent(fileName) + "\"");

        if (decryptionKey != null){
            requestBuilder = withDecryption(requestBuilder, decryptionKey);
        }

        var getReq = requestBuilder.build();
        return s3Client.getObject(getReq, ResponseTransformer.toInputStream());
    }

    private HeadObjectResponse headObject(String fileKey){
        return s3Client.headObject(HeadObjectRequest.builder()
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
    public long getObjectSize(String fileKey){
        return headObject(fileKey).contentLength();
    }

    public void deleteFile(String fileKey){
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build());
    }

    @Override
    public void copyFile(String sourceFileKey, String destinationFileKey) {
        var copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceFileKey)
                .destinationBucket(bucketName)
                .destinationKey(destinationFileKey)
                .build();

        s3Client.copyObject(copyRequest);
    }
}
