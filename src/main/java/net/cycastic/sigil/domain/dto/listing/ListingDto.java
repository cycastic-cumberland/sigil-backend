package net.cycastic.sigil.domain.dto.listing;

import lombok.*;
import net.cycastic.sigil.domain.model.ListingType;
import net.cycastic.sigil.domain.model.listing.Listing;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingDto {
    private int id;
//    private int projectId;
    private String path;
    private ListingType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime removedAt;

    protected void fromListing(Listing listing){
        id = listing.getId();
        path = listing.getListingPath();
        type = listing.getType();
        createdAt = listing.getCreatedAt();
        updatedAt = listing.getUpdatedAt();
        removedAt = listing.getRemovedAt();
    }
}
