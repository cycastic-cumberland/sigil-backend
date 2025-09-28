package net.cycastic.sigil.configuration.storage;

import lombok.Data;

@Data
public class BaseS3Configurations {
    private String regionName;
    private String accessKey;
    private String secretKey;
    private String attachmentBucketName;
    private String serviceUrl;
}
