package net.cycastic.portfoliotoolkit.dto.listing;


import lombok.*;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;
import net.cycastic.portfoliotoolkit.domain.model.listing.TextListing;

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
