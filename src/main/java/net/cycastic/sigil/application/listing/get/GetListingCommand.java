package net.cycastic.sigil.application.listing.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import org.springframework.lang.Nullable;

@Data
public class GetListingCommand implements Command<ListingDto> {
    @NotEmpty
    private String listingPath;
}
