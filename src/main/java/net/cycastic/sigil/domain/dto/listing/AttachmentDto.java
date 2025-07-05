package net.cycastic.sigil.domain.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.model.listing.AttachmentListing;
import net.cycastic.sigil.domain.model.listing.Listing;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttachmentDto extends ListingDto{
    private String bucketName;
    private String bucketRegion;
    private String objectKey;
    private String mimeType;

    public static AttachmentDto fromDomain(AttachmentListing domain, Listing originalListing){
        var dto = new AttachmentDto(domain.getBucketName(), domain.getBucketRegion(), domain.getObjectKey(), domain.getMimeType());
        dto.fromListing(originalListing);

        return dto;
    }
}
