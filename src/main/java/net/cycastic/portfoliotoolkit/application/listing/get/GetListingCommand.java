package net.cycastic.portfoliotoolkit.application.listing.get;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.listing.ListingDto;
import org.springframework.lang.Nullable;

@Data
public class GetListingCommand implements Command<ListingDto> {
    private @Nullable Integer projectId;
    private String listingPath;
}
