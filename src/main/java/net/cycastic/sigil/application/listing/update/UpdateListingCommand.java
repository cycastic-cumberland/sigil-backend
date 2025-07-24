package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class UpdateListingCommand implements Command<Void> {
    private String path;
    private String newPath;
}
