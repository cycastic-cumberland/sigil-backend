package net.cycastic.sigil.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.service.UsageDetails;
import jakarta.annotation.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageDetailsDto implements UsageDetails {
    private @Nullable Integer projectCount;
    private @Nullable Integer lacpCount;
    private @Nullable Integer attachmentCount;
    private @Nullable Long perAttachmentSize;
    private @Nullable Long allAttachmentSize;

    public static UsageDetailsDto from(UsageDetails usageDetails){
        return UsageDetailsDto.builder()
                .projectCount(usageDetails.getProjectCount())
                .lacpCount(usageDetails.getLacpCount())
                .attachmentCount(usageDetails.getAttachmentCount())
                .perAttachmentSize(usageDetails.getPerAttachmentSize())
                .allAttachmentSize(usageDetails.getAllAttachmentSize())
                .build();
    }
}
