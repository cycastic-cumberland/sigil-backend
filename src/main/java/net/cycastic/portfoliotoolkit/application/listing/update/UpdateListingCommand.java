package net.cycastic.portfoliotoolkit.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class UpdateListingCommand implements Command<@Null Object> {
    private String path;
    private String newPath;
}
