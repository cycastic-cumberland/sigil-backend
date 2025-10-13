package net.cycastic.sigil.application.listing.delete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.service.job.BackgroundJob;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteStorageObjectBackgroundJob implements BackgroundJob {
    private String bucketName;
    private String objectKey;
}
