package net.cycastic.sigil.application.listing.update;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.cycastic.sigil.application.misc.transaction.TransactionalCommand;

@Data
@TransactionalCommand
public class UpdateListingCommand implements Command<Void> {
    @NotBlank
    private String path;

    @NotBlank
    private String newPath;
}
