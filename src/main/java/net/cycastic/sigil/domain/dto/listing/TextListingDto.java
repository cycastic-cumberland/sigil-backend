package net.cycastic.sigil.domain.dto.listing;


import lombok.*;
import net.cycastic.sigil.domain.model.listing.Listing;
import net.cycastic.sigil.domain.model.listing.TextListing;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextListingDto extends ListingDto {
    private String text;

    public static TextListingDto fromDomain(TextListing domain, Listing originalListing){
        var text = "";
        if (domain.getTextNormal() != null){
            text = domain.getTextNormal();
        } else if (domain.getTextLong() != null){
            text = domain.getTextLong();
        }

        var dto = new TextListingDto(text);
        dto.fromListing(originalListing);

        return dto;
    }
}
