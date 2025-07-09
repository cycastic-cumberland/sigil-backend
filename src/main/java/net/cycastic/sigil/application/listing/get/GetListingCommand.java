package net.cycastic.sigil.application.listing.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import org.springframework.lang.Nullable;

@Data
public class GetListingCommand implements Command<ListingDto> {
    private String listingPath;
}
