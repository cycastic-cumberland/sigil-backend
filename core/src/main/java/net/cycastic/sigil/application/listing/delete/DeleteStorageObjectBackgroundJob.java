package net.cycastic.sigil.application.listing.delete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteStorageObjectBackgroundJob {
    private String bucketName;
    private String objectKey;
}
