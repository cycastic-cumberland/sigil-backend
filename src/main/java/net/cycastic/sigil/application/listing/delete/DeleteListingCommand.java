package net.cycastic.sigil.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DeleteListingCommand implements Command<Void> {
    @NotEmpty
    private String listingPath;
}
