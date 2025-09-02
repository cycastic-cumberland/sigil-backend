package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@Data
@TransactionalCommand
public class UpdateListingCommand implements Command<Void> {
    @NotEmpty
    private String path;

    @NotEmpty
    private String newPath;
}
