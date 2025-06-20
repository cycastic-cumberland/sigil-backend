package net.cycastic.portfoliotoolkit.application.listing.delete;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class DeleteListingCommand implements Command<@Null Object> {
    private @NotNull String listingPath;
}
