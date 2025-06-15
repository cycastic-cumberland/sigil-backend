package net.cycastic.portfoliotoolkit.domain.dto.listing;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.listing.DecimalListing;
import net.cycastic.portfoliotoolkit.domain.model.listing.Listing;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DecimalListingDto extends ListingDto {
    private BigDecimal number;

    public static DecimalListingDto fromDomain(DecimalListing domain, Listing originalListing){
        var dto = new DecimalListingDto(domain.getNumber());
        dto.fromListing(originalListing);

        return dto;
    }
}
